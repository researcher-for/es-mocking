package org.evomaster.client.java.controller.problem.rpc.schema;

import org.evomaster.client.java.controller.api.dto.problem.rpc.RPCActionDto;
import org.evomaster.client.java.controller.problem.rpc.CodeJavaGenerator;
import org.evomaster.client.java.controller.problem.rpc.schema.params.NamedTypedValue;
import org.evomaster.client.java.controller.problem.rpc.schema.params.StringParam;
import org.evomaster.client.java.controller.problem.rpc.schema.types.AccessibleSchema;
import org.evomaster.client.java.controller.problem.rpc.schema.types.StringType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class LocalAuthSetupSchema extends EndpointSchema{

    public final static String EM_LOCAL_METHOD = "__EM__LOCAL__";
    public final static String HANDLE_LOCAL_AUTHENTICATION_SETUP_METHOD_NAME = "handleLocalAuthenticationSetup";

    public LocalAuthSetupSchema() {
        super(HANDLE_LOCAL_AUTHENTICATION_SETUP_METHOD_NAME,
                EM_LOCAL_METHOD, null, Arrays.asList(new StringParam("arg0", new StringType(), new AccessibleSchema())), null, null, false, null, null);
    }

    /**
     *
     * @return value of AuthenticationInfo
     */
    public String getAuthenticationInfo(){
        return ((StringParam)getRequestParams().get(0)).getValue();
    }

    @Override
    public List<String> newInvocationWithJava(String responseVarName, String controllerVarName, String clientVariable) {
        List<String> javaCode = new ArrayList<>();
        javaCode.add("{");
        int indent = 1;
        for (NamedTypedValue param: getRequestParams()){
            javaCode.addAll(param.newInstanceWithJava(indent));
        }
        String paramVars = getRequestParams().stream().map(NamedTypedValue::getName).collect(Collectors.joining(","));

        CodeJavaGenerator.addCode(
                javaCode,
                CodeJavaGenerator.methodInvocation(controllerVarName, getName(), paramVars) + CodeJavaGenerator.appendLast(),
                indent);

        javaCode.add("}");
        return javaCode;
    }

    /**
     *
     * @param dto a RPCAction dto
     * @return if the action is to local method
     */
    public static boolean isLocalAuthSetup(RPCActionDto dto){
        return dto.actionName.equals(HANDLE_LOCAL_AUTHENTICATION_SETUP_METHOD_NAME) && dto.interfaceId.equals(EM_LOCAL_METHOD) && dto.clientInfo == null;
    }
}
