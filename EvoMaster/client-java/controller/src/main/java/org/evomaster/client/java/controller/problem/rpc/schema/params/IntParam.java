package org.evomaster.client.java.controller.problem.rpc.schema.params;

import org.evomaster.client.java.controller.api.dto.problem.rpc.ParamDto;
import org.evomaster.client.java.controller.api.dto.problem.rpc.RPCSupportedDataType;
import org.evomaster.client.java.controller.problem.rpc.schema.types.AccessibleSchema;
import org.evomaster.client.java.controller.problem.rpc.schema.types.PrimitiveOrWrapperType;

/**
 * int param
 */
public class IntParam extends PrimitiveOrWrapperParam<Integer> {

    public IntParam(String name, String type, String fullTypeName, Class<?> clazz, AccessibleSchema accessibleSchema) {
        super(name, type, fullTypeName, clazz, accessibleSchema);
    }

    public IntParam(String name, PrimitiveOrWrapperType type, AccessibleSchema accessibleSchema) {
        super(name, type, accessibleSchema);
    }

    public IntParam(String name, AccessibleSchema accessibleSchema) {
        super(name, new PrimitiveOrWrapperType(int.class.getSimpleName(), int.class.getName(), false, int.class), accessibleSchema);
    }

    public IntParam(String name){
        this(name, null);
    }


    @Override
    public String getValueAsJavaString() {
        if (getValue() == null)
            return null;
        return ""+getValue();
    }

    @Override
    public ParamDto getDto() {
        ParamDto dto = super.getDto();
        if (getType().isWrapper)
            dto.type.type = RPCSupportedDataType.INT;
        else
            dto.type.type = RPCSupportedDataType.P_INT;

        if (getValue() != null)
            dto.stringValue = getValue().toString();
        return dto;
    }

    @Override
    public IntParam copyStructure() {
        return new IntParam(getName(), getType(), accessibleSchema);
    }


    @Override
    public void setValueBasedOnStringValue(String stringValue) {
        try {
            if (stringValue != null)
                setValue(Integer.parseInt(stringValue));
        }catch (NumberFormatException e){
            throw new RuntimeException("ERROR: fail to convert "+stringValue +" as int value");
        }
    }

    @Override
    protected void setValueBasedOnValidInstance(Object instance) {
        setValue((Integer) instance);
    }

    @Override
    public boolean isValidInstance(Object instance) {
        return instance instanceof Integer;
    }

    @Override
    public String getPrimitiveValue(String responseVarName) {
        if (getType().isWrapper)
            return responseVarName+".intValue()";
        return responseVarName;
    }
}
