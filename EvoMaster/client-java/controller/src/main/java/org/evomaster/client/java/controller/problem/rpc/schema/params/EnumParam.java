package org.evomaster.client.java.controller.problem.rpc.schema.params;

import org.evomaster.client.java.controller.api.dto.problem.rpc.ParamDto;
import org.evomaster.client.java.controller.problem.rpc.CodeJavaGenerator;
import org.evomaster.client.java.controller.problem.rpc.schema.types.AccessibleSchema;
import org.evomaster.client.java.controller.problem.rpc.schema.types.EnumType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * enum parameter
 */
public class EnumParam extends NamedTypedValue<EnumType, Integer> {


    public EnumParam(String name, EnumType type, AccessibleSchema accessibleSchema) {
        super(name, type, accessibleSchema);
    }

    @Override
    public Object newInstance() throws ClassNotFoundException {
        if (getValue() == null)
            return null;
        Class <? extends Enum> clazz = (Class < ? extends Enum >) Class.forName(getType().getFullTypeName());
        String value = getType().getItems()[getValue()];
        return Enum.valueOf(clazz, value);
    }

    @Override
    public ParamDto getDto() {
        ParamDto dto = super.getDto();
        if (getValue() != null)
            dto.stringValue = getValue().toString();
        return dto;
    }

    @Override
    public EnumParam copyStructure() {
        return new EnumParam(getName(), getType(), accessibleSchema);
    }

    @Override
    public void setValueBasedOnDto(ParamDto dto) {
        try {
            if (dto.stringValue != null)
                setValue(Integer.parseInt(dto.stringValue));
        }catch (NumberFormatException e){
            throw new RuntimeException("ERROR: fail to convert "+dto.stringValue +" as int value for setting enum");
        }
    }

    @Override
    protected void setValueBasedOnValidInstance(Object instance) {
        Method m = null;
        try {
            m = instance.getClass().getMethod("ordinal");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("ERROR: fail to process setValueBasedOnValidInstance, with error msg:"+e.getMessage());
        }
        m.setAccessible(true);
        try {
            setValue((int) m.invoke(instance));
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("ERROR: fail to process setValueBasedOnValidInstance, with error msg:"+e.getMessage());
        }
    }

    @Override
    public List<String> newInstanceWithJava(boolean isDeclaration, boolean doesIncludeName, String variableName, int indent) {
        String code;
        if (accessibleSchema != null && accessibleSchema.setterMethodName != null)
            code = CodeJavaGenerator.oneLineSetterInstance(accessibleSchema.setterMethodName, getType().getFullTypeName(), variableName, getValueAsJavaString());
        else{
            if (accessibleSchema != null && !accessibleSchema.isAccessible)
                throw new IllegalStateException("Error: private field, but there is no setter method");
            code = CodeJavaGenerator.oneLineInstance(isDeclaration, doesIncludeName, getType().getFullTypeName(), variableName, getValueAsJavaString());

        }
        return Collections.singletonList(CodeJavaGenerator.getIndent(indent)+ code);
    }

    @Override
    public List<String> newAssertionWithJava(int indent, String responseVarName, int maxAssertionForDataInCollection) {
        StringBuilder sb = new StringBuilder();
        sb.append(CodeJavaGenerator.getIndent(indent));
        if (getValue() == null)
            sb.append(CodeJavaGenerator.junitAssertNull(responseVarName));
        else
            sb.append(CodeJavaGenerator.junitAssertEquals(CodeJavaGenerator.enumValue(getType().getFullTypeName(), getType().getItems()[getValue()]), responseVarName));

        return Collections.singletonList(sb.toString());
    }

    @Override
    public String getValueAsJavaString() {
        if (getValue() == null)
            return null;
        return CodeJavaGenerator.enumValue(getType().getFullTypeName(), getType().getItems()[getValue()]);
    }
}
