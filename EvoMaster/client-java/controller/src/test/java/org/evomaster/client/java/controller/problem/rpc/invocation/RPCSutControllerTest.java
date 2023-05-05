package org.evomaster.client.java.controller.problem.rpc.invocation;

import com.thrift.example.artificial.*;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.path.json.JsonPath;
import io.restassured.response.ResponseBodyExtractionOptions;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocolException;
import org.apache.thrift.transport.TTransportException;
import org.evomaster.client.java.controller.api.Formats;
import org.evomaster.client.java.controller.api.dto.ActionResponseDto;
import org.evomaster.client.java.controller.api.dto.problem.RPCProblemDto;
import org.evomaster.client.java.controller.api.dto.problem.rpc.*;
import org.evomaster.client.java.controller.api.dto.problem.rpc.exception.RPCExceptionType;
import org.evomaster.client.java.controller.problem.rpc.schema.params.NamedTypedValue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


import static io.restassured.RestAssured.given;
import static org.evomaster.client.java.controller.contentMatchers.NumberMatcher.numbersMatch;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

/**
 * created by manzhang on 2021/11/27
 */
public class RPCSutControllerTest {

    public final static FakeSutController rpcController = new FakeSutController();

    private static List<RPCInterfaceSchemaDto> interfaceSchemas;
    private static List<List<RPCActionDto>> seededTestDtos;

    @BeforeAll
    public static void initClass() {
        rpcController.setControllerPort(0);
        rpcController.startTheControllerServer();

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = rpcController.getControllerServerPort();
        RestAssured.basePath = "/controller/api";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();


        RPCProblemDto dto = given()
                .accept(Formats.JSON_V1)
                .get("/infoSUT")
                .then()
                .statusCode(200)
                .body("data.isSutRunning", is(false))
                .extract().body().jsonPath().getObject("data.rpcProblem.", RPCProblemDto.class);

        interfaceSchemas = dto.schemas;
        seededTestDtos = dto.seededTestDtos;
    }

    @AfterAll
    public static void tearDown() {
        rpcController.stopSut();
    }


    @Test
    public void testTypes(){
        List<String> types = interfaceSchemas.get(0).types.stream().map(t-> t.type.fullTypeNameWithGenericType).collect(Collectors.toList());
        assertTrue(types.contains(GenericResponse.class.getName()));
        assertTrue(types.contains(ObjectResponse.class.getName()));
        assertTrue(types.contains(CycleAObj.class.getName()));
        assertTrue(types.contains(CycleBObj.class.getName()));
        assertTrue(types.contains(ConstrainedRequest.class.getName()));
        assertTrue(types.contains(CustomizedRequestA.class.getName()));
        assertTrue(types.contains(CustomizedRequestB.class.getName()));
        assertTrue(types.contains(AuthLoginDto.class.getName()));
        assertTrue(types.contains(PrivateFieldInResponseDto.class.getName()));
        assertTrue(types.contains(ByteResponse.class.getName()));

        assertTrue(types.contains(StringChildDto.class.getName()));
        assertTrue(types.contains(IntChildDto.class.getName()));
        assertTrue(types.contains(ListChildDto.class.getName()));
        assertTrue(types.contains(GenericDto.class.getName()+"<"+Integer.class.getName()+", "+String.class.getName()+">"));
        assertTrue(types.contains(GenericDto.class.getName()+"<"+StringChildDto.class.getName()+", "+String.class.getName()+">"));
        assertTrue(types.contains(NestedGenericDto.class.getName()+"<"+String.class.getName()+">"));
        assertTrue(types.contains(GenericDto.class.getName()+"<"+String.class.getName()+", "+Integer.class.getName()+">"));

        assertTrue(types.contains(BigNumberObj.class.getName()));

    }

    @Test
    public void testIdentifiedTypes(){
        List<String> itypes = Arrays.asList("com.thrift.example.artificial.GenericDto<java.lang.String, java.lang.String>", "com.thrift.example.artificial.GenericDto<java.lang.String, java.lang.Integer>", "com.thrift.example.artificial.NestedStringGenericDto");
        assertNotNull(interfaceSchemas.get(0).identifiedResponseTypes);
        List<ParamDto> idtos = interfaceSchemas.get(0).identifiedResponseTypes;
        assertEquals(3, idtos.size());
        for (String itype : itypes){
            assertTrue(idtos.stream().anyMatch(s-> s.type.fullTypeNameWithGenericType.equals(itype)));
        }
    }

    @Test
    public void testSeedcheck(){

        assertEquals(2, seededTestDtos.size());
        assertEquals(1, seededTestDtos.get(0).size());

        RPCActionDto test_1 = seededTestDtos.get(0).get(0);
        RPCActionDto dto = test_1.copy();

        assertNotNull(dto.mockRPCExternalServiceDtos);
        assertEquals(1, dto.mockRPCExternalServiceDtos.size());
        MockRPCExternalServiceDto mdto = dto.mockRPCExternalServiceDtos.get(0);
        assertEquals(1, mdto.responses.size());
        assertEquals(mdto.responses.size(), mdto.responseTypes.size());



        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "rpcController";
        dto.responseVariable = "res1";
        dto.maxAssertionForDataInCollection = -1;

        ActionResponseDto responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);

        String expectedResponse = "1;2;3;" + System.lineSeparator()+
                "1;2;3;" + System.lineSeparator()+
                "BigNumberObj{bdPositiveFloat=10.12, bdNegativeFloat=-10.12, bdPositiveOrZeroFloat=0.00, bdNegativeOrZeroFloat=-2.16, biPositive=10, biPositiveOrZero=0, biNegative=-10, biNegativeOrZero=-2};" + System.lineSeparator()+
                "1:1;2:2;" + System.lineSeparator();
        assertEquals(expectedResponse, responseDto.rpcResponse.stringValue);


        RPCActionDto test_2 = seededTestDtos.get(1).get(0);
        RPCActionDto dto2 = test_2.copy();

        dto2.doGenerateAssertions = true;
        dto2.doGenerateTestScript = true;
        dto2.controllerVariable = "rpcController";
        dto2.responseVariable = "res1";
        dto2.maxAssertionForDataInCollection = -1;

        ActionResponseDto responseDto2 = new ActionResponseDto();
        rpcController.executeAction(dto2, responseDto2);

        assertEquals("", responseDto2.rpcResponse.stringValue);
    }

    @Test
    public void testPrimitiveResponse(){
        List<String> functions = Arrays.asList("pBoolResponse","pByteResponse","pCharResponse","pShortResponse","pIntResponse","pLongResponse","pFloatResponse","pDoubleResponse");

        List<String> tests = new ArrayList<>();
        int index = 1;
        for (String m : functions){
            List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals(m)).collect(Collectors.toList());
            assertEquals(1, dtos.size());
            RPCActionDto dto = dtos.get(0).copy();
            assertEquals(0, dto.requestParams.size());
            dto.doGenerateAssertions = true;
            dto.doGenerateTestScript = true;
            dto.controllerVariable = "rpcController";
            dto.responseVariable = "res"+index;
            dto.maxAssertionForDataInCollection = -1;

            ActionResponseDto responseDto = new ActionResponseDto();
            rpcController.executeAction(dto, responseDto);

            tests.addAll(responseDto.testScript);
            tests.addAll(responseDto.assertionScript);
            index++;
        }

        String expected ="boolean res1;\n" +
                "{\n" +
                " res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(rpcController.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).pBoolResponse();\n" +
                "}\n" +
                "assertEquals(false, res1);\n" +
                "byte res2;\n" +
                "{\n" +
                " res2 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(rpcController.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).pByteResponse();\n" +
                "}\n" +
                "assertEquals(0, res2);\n" +
                "char res3;\n" +
                "{\n" +
                " res3 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(rpcController.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).pCharResponse();\n" +
                "}\n" +
                "assertEquals('\\u0000', res3);\n" +
                "short res4;\n" +
                "{\n" +
                " res4 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(rpcController.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).pShortResponse();\n" +
                "}\n" +
                "assertEquals(0, res4);\n" +
                "int res5;\n" +
                "{\n" +
                " res5 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(rpcController.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).pIntResponse();\n" +
                "}\n" +
                "assertEquals(0, res5);\n" +
                "long res6;\n" +
                "{\n" +
                " res6 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(rpcController.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).pLongResponse();\n" +
                "}\n" +
                "assertEquals(0L, res6);\n" +
                "float res7;\n" +
                "{\n" +
                " res7 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(rpcController.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).pFloatResponse();\n" +
                "}\n" +
                "assertTrue(numbersMatch(0.0f, res7));\n" +
                "double res8;\n" +
                "{\n" +
                " res8 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(rpcController.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).pDoubleResponse();\n" +
                "}\n" +
                "assertTrue(numbersMatch(0.0, res8));";

        assertEquals(expected, String.join("\n", tests));
    }

    @Test
    public void testMapResponse(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("mapResponse")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(0, dto.requestParams.size());
        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "rpcController";
        dto.responseVariable = "res1";
        dto.maxAssertionForDataInCollection = -1;

        ActionResponseDto responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);

        String expectedTestScript ="java.util.Map<java.lang.String,com.thrift.example.artificial.NumericStringObj> res1 = null;\n" +
                "{\n" +
                " res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(rpcController.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).mapResponse();\n" +
                "}";
        assertEquals(expectedTestScript, String.join("\n", responseDto.testScript));

        String expectedAssertions = "assertEquals(2, res1.size());\n" +
                "assertEquals(\"2L\", res1.get(\"bar\").getLongValue());\n" +
                "assertEquals(\"2\", res1.get(\"bar\").getIntValue());\n" +
                "assertEquals(\"242\", res1.get(\"bar\").getBigIntegerValue());\n" +
                "assertEquals(\"2.42\", res1.get(\"bar\").getBigDecimalValue());\n" +
                "assertEquals(\"42L\", res1.get(\"foo\").getLongValue());\n" +
                "assertEquals(\"42\", res1.get(\"foo\").getIntValue());\n" +
                "assertEquals(\"4242\", res1.get(\"foo\").getBigIntegerValue());\n" +
                "assertEquals(\"42.42\", res1.get(\"foo\").getBigDecimalValue());";

        assertEquals(expectedAssertions, String.join("\n", responseDto.assertionScript));
    }

    @Test
    public void testThriftException(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("throwTException")).collect(Collectors.toList());
        assertEquals(1, dtos.size());

        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(1, dto.requestParams.size());
        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "rpcController";
        dto.responseVariable = "res1";
        dto.maxAssertionForDataInCollection = -1;

        ActionResponseDto responseDto = new ActionResponseDto();
        dto.requestParams.get(0).stringValue = "0";
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.exceptionInfoDto);
        assertEquals(TException.class.getName(), responseDto.exceptionInfoDto.exceptionName);
        assertEquals("Base-TException", responseDto.exceptionInfoDto.exceptionMessage);
        assertEquals(RPCExceptionType.CUSTOMIZED_EXCEPTION, responseDto.exceptionInfoDto.type);

        responseDto = new ActionResponseDto();
        dto.requestParams.get(0).stringValue = "1";
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.exceptionInfoDto);
        assertEquals(TApplicationException.class.getName(), responseDto.exceptionInfoDto.exceptionName);
        assertEquals("TAPP-internal", responseDto.exceptionInfoDto.exceptionMessage);
        assertEquals(RPCExceptionType.APP_INTERNAL_ERROR, responseDto.exceptionInfoDto.type);

        responseDto = new ActionResponseDto();
        dto.requestParams.get(0).stringValue = "2";
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.exceptionInfoDto);
        assertEquals(TApplicationException.class.getName(), responseDto.exceptionInfoDto.exceptionName);
        assertEquals("TAPP-protocol", responseDto.exceptionInfoDto.exceptionMessage);
        assertEquals(RPCExceptionType.APP_PROTOCOL_ERROR, responseDto.exceptionInfoDto.type);

        responseDto = new ActionResponseDto();
        dto.requestParams.get(0).stringValue = "3";
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.exceptionInfoDto);
        assertEquals(TProtocolException.class.getName(), responseDto.exceptionInfoDto.exceptionName);
        assertEquals("TProtocol", responseDto.exceptionInfoDto.exceptionMessage);
        assertEquals(RPCExceptionType.PROTO_UNKNOWN, responseDto.exceptionInfoDto.type);

        responseDto = new ActionResponseDto();
        dto.requestParams.get(0).stringValue = "4";
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.exceptionInfoDto);
        assertEquals(TTransportException.class.getName(), responseDto.exceptionInfoDto.exceptionName);
        assertEquals("TTransport", responseDto.exceptionInfoDto.exceptionMessage);
        assertEquals(RPCExceptionType.TRANS_UNKNOWN, responseDto.exceptionInfoDto.type);


        responseDto = new ActionResponseDto();
        dto.requestParams.get(0).stringValue = "-1";
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.exceptionInfoDto);
        assertEquals(Exception.class.getName(), responseDto.exceptionInfoDto.exceptionName);
        assertEquals("general", responseDto.exceptionInfoDto.exceptionMessage);
        assertEquals(RPCExceptionType.CUSTOMIZED_EXCEPTION, responseDto.exceptionInfoDto.type);
    }

    @Test
    public void testListResponse(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("listResponse")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(0, dto.requestParams.size());
        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "rpcController";
        dto.responseVariable = "res1";
        dto.maxAssertionForDataInCollection = -1;

        ActionResponseDto responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);

        String expectedTestScript ="java.util.List<com.thrift.example.artificial.BigNumberObj> res1 = null;\n" +
                "{\n" +
                " res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(rpcController.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).listResponse();\n" +
                "}";
        assertEquals(expectedTestScript, String.join("\n", responseDto.testScript));

        String expectedAssertions = "assertEquals(1, res1.size());\n" +
                "assertEquals(\"10.12\", res1.get(0).getBdPositiveFloat().toString());\n" +
                "assertEquals(\"-10.12\", res1.get(0).getBdNegativeFloat().toString());\n" +
                "assertEquals(\"0.00\", res1.get(0).getBdPositiveOrZeroFloat().toString());\n" +
                "assertEquals(\"-2.16\", res1.get(0).getBdNegativeOrZeroFloat().toString());\n" +
                "assertEquals(\"10\", res1.get(0).getBiPositive().toString());\n" +
                "assertEquals(\"0\", res1.get(0).getBiPositiveOrZero().toString());\n" +
                "assertEquals(\"-10\", res1.get(0).getBiNegative().toString());\n" +
                "assertEquals(\"-2\", res1.get(0).getBiNegativeOrZero().toString());";
        assertEquals(expectedAssertions, String.join("\n", responseDto.assertionScript));
    }

    @Test
    public void testBigNumber(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("bigNumber")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(1, dto.requestParams.size());
        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "rpcController";
        dto.responseVariable = "res1";

        ActionResponseDto responseDto = new ActionResponseDto();

        ParamDto param = dto.requestParams.get(0);
        param.stringValue = "{}";
        assertEquals(8, param.innerContent.size());
        param.innerContent.get(0).stringValue = "10.12";
        param.innerContent.get(1).stringValue = "-10.12";
        param.innerContent.get(2).stringValue = "0.00";
        param.innerContent.get(3).stringValue = "-2.16";
        param.innerContent.get(4).stringValue = "10";
        param.innerContent.get(5).stringValue = "0";
        param.innerContent.get(6).stringValue = "-10";
        param.innerContent.get(7).stringValue = "-2";

        rpcController.executeAction(dto, responseDto);

        String expect = "BigNumberObj{" +
                "bdPositiveFloat=10.12" +
                ", bdNegativeFloat=-10.12" +
                ", bdPositiveOrZeroFloat=0.00" +
                ", bdNegativeOrZeroFloat=-2.16" +
                ", biPositive=10" +
                ", biPositiveOrZero=0" +
                ", biNegative=-10" +
                ", biNegativeOrZero=-2" +
                '}';
        assertEquals(expect, responseDto.rpcResponse.stringValue);

        List<String> assertionScript = responseDto.assertionScript;
        assertEquals("//assertEquals(\"BigNumberObj{bdPositiveFloat=10.12, bdNegativeFloat=-10.12, bdPositiveOrZeroFloat=0.00, bdNegativeOrZeroFloat=-2.16, biPositive=10, biPositiveOrZero=0, biNegative=-10, biNegativeOrZero=-2}\", res1);", assertionScript.get(0));
    }

    @Test
    public void testEnumWithConstructor(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("handleEnumWithConstructor")).collect(Collectors.toList());

        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(1, dto.requestParams.size());
        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "rpcController";
        dto.responseVariable = "res1";

        ActionResponseDto responseDto = new ActionResponseDto();

        ParamDto param = dto.requestParams.get(0);
        param.stringValue = "{}";
        param.innerContent.get(0).stringValue="0";
        rpcController.executeAction(dto, responseDto);
        assertNull(responseDto.exceptionInfoDto);
        assertEquals(9, responseDto.testScript.size());
        assertEquals(1, responseDto.assertionScript.size());
        assertEquals("first", responseDto.rpcResponse.stringValue);
    }

    @Test
    public void testJavaException(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("handleException")).collect(Collectors.toList());

        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(1, dto.requestParams.size());

        ActionResponseDto responseDto = new ActionResponseDto();

        ParamDto param = dto.requestParams.get(0);
        param.stringValue = null;
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.exceptionInfoDto);
        assertEquals(NullPointerException.class.getName(), responseDto.exceptionInfoDto.exceptionName);
        assertEquals("null", responseDto.exceptionInfoDto.exceptionMessage);

        param.stringValue = "state";
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.exceptionInfoDto);
        assertEquals(IllegalStateException.class.getName(), responseDto.exceptionInfoDto.exceptionName);
        assertEquals("state", responseDto.exceptionInfoDto.exceptionMessage);

        param.stringValue = "argument";
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.exceptionInfoDto);
        assertEquals(IllegalArgumentException.class.getName(), responseDto.exceptionInfoDto.exceptionName);
        assertEquals("argument", responseDto.exceptionInfoDto.exceptionMessage);

        param.stringValue = "foo";
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.exceptionInfoDto);
        assertEquals(RuntimeException.class.getName(), responseDto.exceptionInfoDto.exceptionName);
        assertEquals("foo", responseDto.exceptionInfoDto.exceptionMessage);
    }

    @Test
    public void testObjectResponseAssertion(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("objResponse")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(0, dto.requestParams.size());
        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "rpcController";
        dto.responseVariable = "res1";

        ActionResponseDto responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);

        assertEquals(4, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.ObjectResponse res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(rpcController.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).objResponse();", responseDto.testScript.get(2));
        assertEquals("}", responseDto.testScript.get(3));

        assertEquals(7, responseDto.assertionScript.size());
        assertEquals("assertEquals(\"foo\", res1.f1);", responseDto.assertionScript.get(0));
        assertEquals("assertEquals(42, res1.f2);", responseDto.assertionScript.get(1));
        assertEquals("assertTrue(numbersMatch(0.42, res1.f3));", responseDto.assertionScript.get(2));
        assertEquals("assertNull(res1.cycle);", responseDto.assertionScript.get(3));
        assertEquals("assertEquals(3, res1.f4.length);", responseDto.assertionScript.get(4));
        assertEquals("assertNull(res1.f5);", responseDto.assertionScript.get(5));
        assertTrue(responseDto.assertionScript.get(6).contains("//assertEquals"));
    }

    @Test
    public void testHandleNestedGenericString(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("handleNestedGenericString")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(1, dto.requestParams.size());
        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "rpcController";
        dto.responseVariable = "res1";

        ActionResponseDto responseDto = new ActionResponseDto();
        dto.requestParams.get(0).innerContent = null;
        rpcController.executeAction(dto, responseDto);


        assertEquals(5, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.NestedGenericDto<java.lang.String> res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.NestedGenericDto<java.lang.String> arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(rpcController.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handleNestedGenericString(arg0);", responseDto.testScript.get(3));
        assertEquals("}", responseDto.testScript.get(4));

        assertNotNull(responseDto.exceptionInfoDto);
        assertEquals("java.lang.NullPointerException", responseDto.exceptionInfoDto.exceptionName);

        dto = dtos.get(0).copy();
        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "rpcController";
        dto.responseVariable = "res1";
        dto.maxAssertionForDataInCollection = 4;

        ParamDto request = dto.requestParams.get(0);
        request.stringValue = "{}";

        responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);

        request.stringValue = "{}";
        request.innerContent.get(0).innerContent = null;
        request.innerContent.get(1).innerContent = null;
        request.innerContent.get(2).innerContent = null;
        rpcController.executeAction(dto, responseDto);


        assertEquals(11, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.NestedGenericDto<java.lang.String> res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.NestedGenericDto<java.lang.String> arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" {", responseDto.testScript.get(3));
        assertEquals("  arg0 = new com.thrift.example.artificial.NestedGenericDto<java.lang.String>();", responseDto.testScript.get(4));
        assertEquals("  arg0.intData = null;", responseDto.testScript.get(5));
        assertEquals("  arg0.stringData = null;", responseDto.testScript.get(6));
        assertEquals("  arg0.list = null;", responseDto.testScript.get(7));
        assertEquals(" }", responseDto.testScript.get(8));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(rpcController.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handleNestedGenericString(arg0);", responseDto.testScript.get(9));
        assertEquals("}", responseDto.testScript.get(10));

        assertEquals("assertEquals(\"child\", res1.intData.data1);", responseDto.assertionScript.get(0));
        assertEquals("assertEquals(0, res1.intData.data2.intValue());", responseDto.assertionScript.get(1));
        assertEquals("assertEquals(\"child\", res1.stringData.data1);", responseDto.assertionScript.get(2));
        assertEquals("assertEquals(\"child\", res1.stringData.data2);", responseDto.assertionScript.get(3));
        assertEquals("assertEquals(2, res1.list.size());", responseDto.assertionScript.get(4));
        assertEquals("assertEquals(\"child\", res1.list.get(0));", responseDto.assertionScript.get(5));
        assertEquals("assertEquals(\"child\", res1.list.get(1));", responseDto.assertionScript.get(6));

    }

    @Test
    public void testHandleGenericIntString(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("handleGenericIntString")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(1, dto.requestParams.size());
        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "controller";
        dto.responseVariable = "res1";

        ActionResponseDto responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);

        assertEquals(5, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.GenericDto<java.lang.Integer, java.lang.String> res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.GenericDto<java.lang.Integer, java.lang.String> arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(controller.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handleGenericIntString(arg0);", responseDto.testScript.get(3));
        assertEquals("}", responseDto.testScript.get(4));

        assertEquals(1, responseDto.assertionScript.size());
        assertEquals("assertNull(res1);", responseDto.assertionScript.get(0));

        ParamDto request = dto.requestParams.get(0);
        request.stringValue = "{}";

        responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);


        assertEquals(10, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.GenericDto<java.lang.Integer, java.lang.String> res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.GenericDto<java.lang.Integer, java.lang.String> arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" {", responseDto.testScript.get(3));
        assertEquals("  arg0 = new com.thrift.example.artificial.GenericDto<java.lang.Integer, java.lang.String>();", responseDto.testScript.get(4));
        assertEquals("  arg0.data1 = null;", responseDto.testScript.get(5));
        assertEquals("  arg0.data2 = null;", responseDto.testScript.get(6));
        assertEquals(" }", responseDto.testScript.get(7));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(controller.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handleGenericIntString(arg0);", responseDto.testScript.get(8));
        assertEquals("}", responseDto.testScript.get(9));

        responseDto.assertionScript.forEach(System.out::println);
        assertEquals(2, responseDto.assertionScript.size());
        assertEquals("assertEquals(0, res1.data1.intValue());", responseDto.assertionScript.get(0));
        assertEquals("assertEquals(\"generic\", res1.data2);", responseDto.assertionScript.get(1));

    }

    @Test
    public void testHandleGenericObjectString(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("handleGenericObjectString")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(1, dto.requestParams.size());
        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "controller";
        dto.responseVariable = "res1";

        ActionResponseDto responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);

        assertEquals(5, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.GenericDto<com.thrift.example.artificial.StringChildDto, java.lang.String> res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.GenericDto<com.thrift.example.artificial.StringChildDto, java.lang.String> arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(controller.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handleGenericObjectString(arg0);", responseDto.testScript.get(3));
        assertEquals("}", responseDto.testScript.get(4));

        assertEquals(1, responseDto.assertionScript.size());
        assertEquals("assertNull(res1);", responseDto.assertionScript.get(0));

        ParamDto request = dto.requestParams.get(0);
        request.stringValue = "{}";
        request.innerContent.get(0).stringValue ="{}";
        responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);

        assertEquals(15, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.GenericDto<com.thrift.example.artificial.StringChildDto, java.lang.String> res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.GenericDto<com.thrift.example.artificial.StringChildDto, java.lang.String> arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" {", responseDto.testScript.get(3));
        assertEquals("  arg0 = new com.thrift.example.artificial.GenericDto<com.thrift.example.artificial.StringChildDto, java.lang.String>();", responseDto.testScript.get(4));
        assertEquals("  arg0.data1 = null;", responseDto.testScript.get(5));
        assertEquals("  {", responseDto.testScript.get(6));
        assertEquals("   arg0.data1 = new com.thrift.example.artificial.StringChildDto();", responseDto.testScript.get(7));
        assertEquals("   arg0.data1.setCode(null);", responseDto.testScript.get(8));
        assertEquals("   arg0.data1.setMessage(null);", responseDto.testScript.get(9));
        assertEquals("  }", responseDto.testScript.get(10));
        assertEquals("  arg0.data2 = null;", responseDto.testScript.get(11));
        assertEquals(" }", responseDto.testScript.get(12));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(controller.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handleGenericObjectString(arg0);", responseDto.testScript.get(13));
        assertEquals("}", responseDto.testScript.get(14));

        assertEquals(3, responseDto.assertionScript.size());
        assertEquals("assertEquals(\"child\", res1.data1.getCode());", responseDto.assertionScript.get(0));
        assertEquals("assertEquals(\"child\", res1.data1.getMessage());", responseDto.assertionScript.get(1));
        assertEquals("assertEquals(\"generic\", res1.data2);", responseDto.assertionScript.get(2));
    }


    @Test
    public void testHandledInheritedGenericStringDto(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("handledInheritedGenericStringDto")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(1, dto.requestParams.size());
        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "controller";
        dto.responseVariable = "res1";

        ActionResponseDto responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);


        assertEquals(5, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.StringChildDto res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.StringChildDto arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(controller.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handledInheritedGenericStringDto(arg0);", responseDto.testScript.get(3));
        assertEquals("}", responseDto.testScript.get(4));

        assertEquals(1, responseDto.assertionScript.size());
        assertEquals("assertNull(res1);", responseDto.assertionScript.get(0));

        ParamDto request = dto.requestParams.get(0);
        request.stringValue = "{}";

        responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);

        assertEquals(10, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.StringChildDto res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.StringChildDto arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" {", responseDto.testScript.get(3));
        assertEquals("  arg0 = new com.thrift.example.artificial.StringChildDto();", responseDto.testScript.get(4));
        assertEquals("  arg0.setCode(null);", responseDto.testScript.get(5));
        assertEquals("  arg0.setMessage(null);", responseDto.testScript.get(6));
        assertEquals(" }", responseDto.testScript.get(7));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(controller.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handledInheritedGenericStringDto(arg0);", responseDto.testScript.get(8));
        assertEquals("}", responseDto.testScript.get(9));

        assertEquals(2, responseDto.assertionScript.size());
        assertEquals("assertEquals(\"child\", res1.getCode());", responseDto.assertionScript.get(0));
        assertEquals("assertEquals(\"child\", res1.getMessage());", responseDto.assertionScript.get(1));

        request = dto.requestParams.get(0);
        assertEquals(2, request.innerContent.size());
        request.innerContent.get(0).stringValue = "ppcode";
        request.innerContent.get(1).stringValue = "pmsg";

        responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);

        assertEquals(10, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.StringChildDto res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.StringChildDto arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" {", responseDto.testScript.get(3));
        assertEquals("  arg0 = new com.thrift.example.artificial.StringChildDto();", responseDto.testScript.get(4));
        assertEquals("  arg0.setCode(\"ppcode\");", responseDto.testScript.get(5));
        assertEquals("  arg0.setMessage(\"pmsg\");", responseDto.testScript.get(6));
        assertEquals(" }", responseDto.testScript.get(7));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(controller.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handledInheritedGenericStringDto(arg0);", responseDto.testScript.get(8));
        assertEquals("}", responseDto.testScript.get(9));

        assertEquals(2, responseDto.assertionScript.size());
        assertEquals("//assertEquals(\"childppcode\", res1.getCode());", responseDto.assertionScript.get(0));
        assertEquals("//assertEquals(\"childpmsg\", res1.getMessage());", responseDto.assertionScript.get(1));
    }


    @Test
    public void testHandledInheritedGenericIntDto(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("handledInheritedGenericIntDto")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(1, dto.requestParams.size());
        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "controller";
        dto.responseVariable = "res1";

        ActionResponseDto responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);


        assertEquals(5, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.IntChildDto res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.IntChildDto arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(controller.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handledInheritedGenericIntDto(arg0);", responseDto.testScript.get(3));
        assertEquals("}", responseDto.testScript.get(4));

        assertEquals(1, responseDto.assertionScript.size());
        assertEquals("assertNull(res1);", responseDto.assertionScript.get(0));


        ParamDto request = dto.requestParams.get(0);
        assertEquals(2, request.innerContent.size());
        request.stringValue = "{}";

        responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);


        assertEquals(10, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.IntChildDto res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.IntChildDto arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" {", responseDto.testScript.get(3));
        assertEquals("  arg0 = new com.thrift.example.artificial.IntChildDto();", responseDto.testScript.get(4));
        assertEquals("  arg0.setCode(null);", responseDto.testScript.get(5));
        assertEquals("  arg0.setMessage(null);", responseDto.testScript.get(6));
        assertEquals(" }", responseDto.testScript.get(7));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(controller.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handledInheritedGenericIntDto(arg0);", responseDto.testScript.get(8));
        assertEquals("}", responseDto.testScript.get(9));

        assertEquals(2, responseDto.assertionScript.size());
        assertEquals("assertEquals(0, res1.getCode().intValue());", responseDto.assertionScript.get(0));
        assertEquals("assertEquals(0, res1.getMessage().intValue());", responseDto.assertionScript.get(1));

        request = dto.requestParams.get(0);
        assertEquals(2, request.innerContent.size());
        request.innerContent.get(0).stringValue = "1";
        request.innerContent.get(1).stringValue = "2";

        responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);

        assertEquals(10, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.IntChildDto res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.IntChildDto arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" {", responseDto.testScript.get(3));
        assertEquals("  arg0 = new com.thrift.example.artificial.IntChildDto();", responseDto.testScript.get(4));
        assertEquals("  arg0.setCode(1);", responseDto.testScript.get(5));
        assertEquals("  arg0.setMessage(2);", responseDto.testScript.get(6));
        assertEquals(" }", responseDto.testScript.get(7));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(controller.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handledInheritedGenericIntDto(arg0);", responseDto.testScript.get(8));
        assertEquals("}", responseDto.testScript.get(9));

        assertEquals(2, responseDto.assertionScript.size());
        assertEquals("assertEquals(2, res1.getCode().intValue());", responseDto.assertionScript.get(0));
        assertEquals("assertEquals(3, res1.getMessage().intValue());", responseDto.assertionScript.get(1));
    }


    @Test
    public void testHandledInheritedGenericListDto(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("handledInheritedGenericListDto")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(1, dto.requestParams.size());
        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "controller";
        dto.responseVariable = "res1";
        dto.maxAssertionForDataInCollection = 10;

        ActionResponseDto responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);


        assertEquals(5, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.ListChildDto res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.ListChildDto arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(controller.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handledInheritedGenericListDto(arg0);", responseDto.testScript.get(3));
        assertEquals("}", responseDto.testScript.get(4));

        assertEquals(1, responseDto.assertionScript.size());
        assertEquals("assertNull(res1);", responseDto.assertionScript.get(0));

        ParamDto request = dto.requestParams.get(0);
        request.stringValue = "{}";

        responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);

        assertEquals(12, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.ListChildDto res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.ListChildDto arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" {", responseDto.testScript.get(3));
        assertEquals("  arg0 = new com.thrift.example.artificial.ListChildDto();", responseDto.testScript.get(4));
        assertEquals("  java.util.List<java.lang.Integer> arg0_code = null;", responseDto.testScript.get(5));
        assertEquals("  arg0.setCode(arg0_code);", responseDto.testScript.get(6));
        assertEquals("  java.util.List<java.lang.Integer> arg0_message = null;", responseDto.testScript.get(7));
        assertEquals("  arg0.setMessage(arg0_message);", responseDto.testScript.get(8));
        assertEquals(" }", responseDto.testScript.get(9));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(controller.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handledInheritedGenericListDto(arg0);", responseDto.testScript.get(10));
        assertEquals("}", responseDto.testScript.get(11));

        assertEquals(4, responseDto.assertionScript.size());
        assertEquals("assertEquals(1, res1.getCode().size());", responseDto.assertionScript.get(0));
        assertEquals("assertEquals(0, res1.getCode().get(0).intValue());", responseDto.assertionScript.get(1));
        assertEquals("assertEquals(1, res1.getMessage().size());", responseDto.assertionScript.get(2));
        assertEquals("assertEquals(0, res1.getMessage().get(0).intValue());", responseDto.assertionScript.get(3));

        request = dto.requestParams.get(0);
        assertEquals(2, request.innerContent.size());
        ParamDto innerCode = request.innerContent.get(0).type.example.copy();
        innerCode.stringValue = "1";
        request.innerContent.get(0).innerContent = Arrays.asList(innerCode);
        ParamDto innerMsg = request.innerContent.get(1).type.example.copy();
        innerMsg.stringValue = "2";
        request.innerContent.get(1).innerContent = Arrays.asList(innerMsg);

        responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);

        assertEquals(22, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.ListChildDto res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" com.thrift.example.artificial.ListChildDto arg0 = null;", responseDto.testScript.get(2));
        assertEquals(" {", responseDto.testScript.get(3));
        assertEquals("  arg0 = new com.thrift.example.artificial.ListChildDto();", responseDto.testScript.get(4));
        assertEquals("  java.util.List<java.lang.Integer> arg0_code = null;", responseDto.testScript.get(5));
        assertEquals("  {", responseDto.testScript.get(6));
        assertEquals("   arg0_code = new java.util.ArrayList<>();", responseDto.testScript.get(7));
        assertEquals("   java.lang.Integer arg0_code_e_0 = 1;", responseDto.testScript.get(8));
        assertEquals("   arg0_code.add(arg0_code_e_0);", responseDto.testScript.get(9));
        assertEquals("  }", responseDto.testScript.get(10));
        assertEquals("  arg0.setCode(arg0_code);", responseDto.testScript.get(11));
        assertEquals("  java.util.List<java.lang.Integer> arg0_message = null;", responseDto.testScript.get(12));
        assertEquals("  {", responseDto.testScript.get(13));
        assertEquals("   arg0_message = new java.util.ArrayList<>();", responseDto.testScript.get(14));
        assertEquals("   java.lang.Integer arg0_message_e_0 = 2;", responseDto.testScript.get(15));
        assertEquals("   arg0_message.add(arg0_message_e_0);", responseDto.testScript.get(16));
        assertEquals("  }", responseDto.testScript.get(17));
        assertEquals("  arg0.setMessage(arg0_message);", responseDto.testScript.get(18));
        assertEquals(" }", responseDto.testScript.get(19));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(controller.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).handledInheritedGenericListDto(arg0);", responseDto.testScript.get(20));
        assertEquals("}", responseDto.testScript.get(21));

        assertEquals(4, responseDto.assertionScript.size());
        assertEquals("assertEquals(1, res1.getCode().size());", responseDto.assertionScript.get(0));
        assertEquals("assertEquals(2, res1.getCode().get(0).intValue());", responseDto.assertionScript.get(1));
        assertEquals("assertEquals(1, res1.getMessage().size());", responseDto.assertionScript.get(2));
        assertEquals("assertEquals(3, res1.getMessage().get(0).intValue());", responseDto.assertionScript.get(3));
    }


    @Test
    public void testRuntimeException(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("throwRuntimeException")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        ActionResponseDto responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.exceptionInfoDto);
        assertEquals("java.lang.RuntimeException", responseDto.exceptionInfoDto.exceptionName);
        assertEquals("runtime exception", responseDto.exceptionInfoDto.exceptionMessage);
        assertFalse(responseDto.exceptionInfoDto.isCauseOfUndeclaredThrowable);
    }

    @Test
    public void testUndeclaredThrowableException(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("throwUndeclaredThrowableException")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        ActionResponseDto responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.exceptionInfoDto);
        assertEquals("java.lang.IllegalStateException", responseDto.exceptionInfoDto.exceptionName);
        assertEquals("undeclared", responseDto.exceptionInfoDto.exceptionMessage);
        assertTrue(responseDto.exceptionInfoDto.isCauseOfUndeclaredThrowable);
    }

    @Test
    public void testLocalAuth(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("authorizedEndpoint")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        RPCActionDto localDto = rpcController.getLocalAuthSetupSchemaMap().get(0).getDto();

        localDto.responseVariable = "res1_auth";
        localDto.doGenerateTestScript = true;
        localDto.controllerVariable = "controller";
        ActionResponseDto authResponseDto = new ActionResponseDto();
        rpcController.executeHandleLocalAuthenticationSetup(localDto, authResponseDto);
        assertNotNull(authResponseDto.testScript);
        assertEquals(4, authResponseDto.testScript.size());
        assertEquals("{", authResponseDto.testScript.get(0));
        assertEquals(" java.lang.String arg0 = \"local_foo\";", authResponseDto.testScript.get(1));
        assertEquals(" controller.handleLocalAuthenticationSetup(arg0);", authResponseDto.testScript.get(2));
        assertEquals("}", authResponseDto.testScript.get(3));


        dto.doGenerateAssertions = true;
        dto.doGenerateTestScript = true;
        dto.controllerVariable = "controller";
        dto.responseVariable = "res1";
        ActionResponseDto responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.assertionScript);
        assertEquals("assertEquals(\"local\", res1);", responseDto.assertionScript.get(0));

    }

    @Test
    public void testSutInfoAndSchema(){
        assertEquals(1, interfaceSchemas.size());
    }

    @Test
    public void testSimpleWrapPrimitiveEndpoint(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("simpleWrapPrimitive")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(8, dto.requestParams.size());
        dto.requestParams.get(0).stringValue = ""+42;
        dto.requestParams.get(1).stringValue = ""+4.2f;
        dto.requestParams.get(2).stringValue = ""+42L;
        dto.requestParams.get(3).stringValue = ""+4.2;
        dto.requestParams.get(4).stringValue = ""+'x';
        dto.requestParams.get(5).stringValue = ""+ Byte.parseByte("42");
        dto.requestParams.get(6).stringValue = ""+ false;
        dto.requestParams.get(7).stringValue = ""+ Short.parseShort("42");
        ActionResponseDto responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.rpcResponse);
        assertEquals(RPCSupportedDataType.STRING, responseDto.rpcResponse.type.type);
        assertEquals("int:42,float:4.2,long:42,double:4.2,char:x,byte:42,boolean:false,short:42", responseDto.rpcResponse.stringValue);
    }

    @Test
    public void testSimplePrimitiveEndpoint(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("simplePrimitive")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        assertEquals(8, dto.requestParams.size());
        dto.requestParams.get(0).stringValue = ""+42;
        dto.requestParams.get(1).stringValue = ""+4.2f;
        dto.requestParams.get(2).stringValue = ""+42L;
        dto.requestParams.get(3).stringValue = ""+4.2;
        dto.requestParams.get(4).stringValue = ""+'x';
        dto.requestParams.get(5).stringValue = ""+ Byte.parseByte("42");
        dto.requestParams.get(6).stringValue = ""+ false;
        dto.requestParams.get(7).stringValue = ""+ Short.parseShort("42");
        ActionResponseDto responseDto = new ActionResponseDto();
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.rpcResponse);
        assertEquals(RPCSupportedDataType.STRING, responseDto.rpcResponse.type.type);
        assertEquals("int:42,float:4.2,long:42,double:4.2,char:x,byte:42,boolean:false,short:42", responseDto.rpcResponse.stringValue);
    }


    @Test
    public void testByteResponse(){
        List<RPCActionDto> dtos = interfaceSchemas.get(0).endpoints.stream().filter(s-> s.actionName.equals("byteResponse")).collect(Collectors.toList());
        assertEquals(1, dtos.size());
        RPCActionDto dto = dtos.get(0).copy();
        dto.requestParams.get(0).stringValue = "" + Byte.parseByte("0");
        dto.requestParams.get(1).stringValue = "" + Byte.parseByte("42");
        assertEquals(2, dto.requestParams.size());
        ActionResponseDto responseDto = new ActionResponseDto();
        dto.doGenerateTestScript = true;
        dto.doGenerateAssertions = true;
        dto.controllerVariable = "controller";
        dto.responseVariable = "res1";
        rpcController.executeAction(dto, responseDto);
        assertNotNull(responseDto.rpcResponse);
        assertEquals(RPCSupportedDataType.CUSTOM_OBJECT, responseDto.rpcResponse.type.type);
        assertEquals(6, responseDto.testScript.size());
        assertEquals("com.thrift.example.artificial.ByteResponse res1 = null;", responseDto.testScript.get(0));
        assertEquals("{", responseDto.testScript.get(1));
        assertEquals(" byte arg0 = 0;", responseDto.testScript.get(2));
        assertEquals(" java.lang.Byte arg1 = 42;", responseDto.testScript.get(3));
        assertEquals(" res1 = ((com.thrift.example.artificial.RPCInterfaceExampleImpl)(controller.getRPCClient(\"com.thrift.example.artificial.RPCInterfaceExample\"))).byteResponse(arg0,arg1);", responseDto.testScript.get(4));
        assertEquals("}", responseDto.testScript.get(5));
        assertEquals(2, responseDto.assertionScript.size());
        assertEquals("assertEquals(42, res1.byteValue.byteValue());", responseDto.assertionScript.get(0));
        assertEquals("assertEquals(0, res1.pbyteValue);", responseDto.assertionScript.get(1));
        responseDto.testScript.forEach(System.out::println);
        responseDto.assertionScript.forEach(System.out::println);

    }
}
