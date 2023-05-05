package em.external.org.ind1;

import org.evomaster.client.java.controller.ExternalSutController;
import org.evomaster.client.java.controller.InstrumentedSutStarter;
import org.evomaster.client.java.controller.api.dto.AuthenticationDto;
import org.evomaster.client.java.controller.api.dto.SutInfoDto;
import org.evomaster.client.java.controller.api.dto.database.schema.DatabaseType;
import org.evomaster.client.java.controller.db.DbCleaner;
import org.evomaster.client.java.controller.db.SqlScriptRunnerCached;
import org.evomaster.client.java.controller.internal.db.DbSpecification;
import org.evomaster.client.java.controller.problem.ProblemInfo;
import org.evomaster.client.java.controller.problem.RestProblem;
import org.testcontainers.containers.GenericContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ExternalEvoMasterController extends ExternalSutController {

    /*
        All info that could possibly identify the case study is removed, and
        must be passed as either parameter input or environment variable
     */
    private static final String SUT_LOCATION_IND1 = System.getenv("SUT_LOCATION_IND1");
    private static final String SUT_PACKAGE_IND1 = System.getenv("SUT_PACKAGE_IND1");

    public static void main(String[] args) {

        int controllerPort = 40100;
        if (args.length > 0) {
            controllerPort = Integer.parseInt(args[0]);
        }
        int sutPort = 12345;
        if (args.length > 1) {
            sutPort = Integer.parseInt(args[1]);
        }
        String jarLocation = SUT_LOCATION_IND1;
        if (args.length > 2) {
            jarLocation = args[2];
        }

        int timeoutSeconds = 120;
        if(args.length > 3){
            timeoutSeconds = Integer.parseInt(args[3]);
        }

        String packagesToInstrument = SUT_PACKAGE_IND1;
        if(args.length > 5){
            packagesToInstrument = args[5];
        }

        String command = "java";
        if(args.length > 4){
            command = args[4];
        }


        ExternalEvoMasterController controller =
                new ExternalEvoMasterController(controllerPort, jarLocation,
                        sutPort, timeoutSeconds, command, packagesToInstrument);
        InstrumentedSutStarter starter = new InstrumentedSutStarter(controller);

        starter.start();
    }

    private final int timeoutSeconds;
    private final int sutPort;
    private  String jarLocation;
    private final String packagesToInstrument;
    private Connection sqlConnection;
    private List<DbSpecification> dbSpecification;

    private static final GenericContainer postgres = new GenericContainer("postgres:9")
            .withExposedPorts(5432)
            .withEnv("POSTGRES_HOST_AUTH_METHOD","trust")
            .withTmpFs(Collections.singletonMap("/var/lib/postgresql/data", "rw"));

    public ExternalEvoMasterController(){
        this(40100, SUT_LOCATION_IND1, 12345, 120, "java", SUT_PACKAGE_IND1);
    }

    public ExternalEvoMasterController(String jarLocation) {
        this();
        this.jarLocation = jarLocation;
    }

    public ExternalEvoMasterController(
            int controllerPort, String jarLocation, int sutPort, int timeoutSeconds, String command,
            String packagesToInstrument) {

        if(jarLocation==null || jarLocation.isEmpty()){
            throw new IllegalArgumentException("Missing jar location");
        }
        if(packagesToInstrument==null || packagesToInstrument.isEmpty()){
            throw new IllegalArgumentException("Missing packages to instrument");
        }

        this.sutPort = sutPort;
        this.jarLocation = jarLocation;
        this.timeoutSeconds = timeoutSeconds;
        this.packagesToInstrument = packagesToInstrument;
        setControllerPort(controllerPort);
        setJavaCommand(command);
    }


    @Override
    public String[] getInputParameters() {
        return new String[]{
                "--server.port=" + sutPort,
                "--spring.datasource.url="+dbUrl(),
                "--spring.datasource.username=postgres",
                "--spring.datasource.password",
                "--spring.cache.type=none",
                "--spring.jmx.enabled=false",
                "--spring.profiles.active=test",
                "--subscriptions.slack.notification_url=https://hooks.slack.com/services/fake/fake/fake",
                "--auth0.authorization.api_url=http://fakeaddressdoesnotexist.no/adf6e2f2b84784b57522e3b19dfc9201/api",
                "--auth0.subscriptions.client_id=fooSubscriptionsId",
                "--auth0.domain=foo-test.eu.auth0.com"
        };
    }

    public String[] getJVMParameters() {

        return new String[]{
                "-Xmx4G"
        };
    }

    private String dbUrl( ) {

        String host = postgres.getContainerIpAddress();
        int port = postgres.getMappedPort(5432);

        String url = "jdbc";
        url += ":postgresql://"+host+":"+port+"/postgres?currentSchema=subscriptions";

        return url;
    }

    @Override
    public String getBaseURL() {
        return "http://localhost:" + sutPort;
    }

    @Override
    public String getPathToExecutableJar() {
        return jarLocation;
    }

    @Override
    public String getLogMessageOfInitializedServer() {
        return "Tomcat started on port";
    }

    @Override
    public long getMaxAwaitForInitializationInSeconds() {
        return timeoutSeconds;
    }

    @Override
    public void preStart() {
        postgres.start();
    }

    @Override
    public void postStart() {
        closeDataBaseConnection();

        try {
            sqlConnection = DriverManager.getConnection(dbUrl(), "postgres", "");
            dbSpecification = Arrays.asList(new DbSpecification(DatabaseType.POSTGRES,sqlConnection)
                    .withSchemas("subscriptions").withDisabledSmartClean());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void resetStateOfSUT() {
        DbCleaner.clearDatabase_Postgres(sqlConnection,
                "subscriptions",
                Arrays.asList("flyway_schema_history"));
        SqlScriptRunnerCached.runScriptFromResourceFile(sqlConnection,"/init_db.sql");
    }

    @Override
    public void preStop() {
        closeDataBaseConnection();
    }

    @Override
    public void postStop() {
        postgres.stop();
    }

    private void closeDataBaseConnection() {
        if (sqlConnection != null) {
            try {
                sqlConnection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            sqlConnection = null;
        }
    }

    @Override
    public String getPackagePrefixesToCover() {
        return packagesToInstrument;
    }

    @Override
    public ProblemInfo getProblemInfo() {
        return new RestProblem(
                getBaseURL() + "/v3/api-docs",
                null
        );
    }

    @Override
    public SutInfoDto.OutputFormat getPreferredOutputFormat() {
        return SutInfoDto.OutputFormat.JAVA_JUNIT_4;
    }


    @Override
    public List<AuthenticationDto> getInfoForAuthentication() {
        return null;
    }

    @Override
    public List<DbSpecification> getDbSpecifications() {
        return dbSpecification;
    }
}
