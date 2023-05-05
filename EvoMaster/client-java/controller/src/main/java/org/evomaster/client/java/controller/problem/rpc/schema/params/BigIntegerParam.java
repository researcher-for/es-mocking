package org.evomaster.client.java.controller.problem.rpc.schema.params;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.evomaster.client.java.controller.api.dto.problem.rpc.ParamDto;
import org.evomaster.client.java.controller.problem.rpc.CodeJavaGenerator;
import org.evomaster.client.java.controller.problem.rpc.schema.types.AccessibleSchema;
import org.evomaster.client.java.controller.problem.rpc.schema.types.BigIntegerType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * info for JDK:
 *      BigInteger constructors and operations throw ArithmeticException when the result is
 *      out of the supported range of -2^Integer.MAX_VALUE (exclusive) to +2^Integer.MAX_VALUE (exclusive).
 */
public class BigIntegerParam extends NamedTypedValue<BigIntegerType, BigInteger> implements NumericConstraintBase<BigInteger> {

    private BigInteger min;

    private BigInteger max;

    private boolean minInclusive = true;

    private boolean maxInclusive = true;

    /**
     * constraints with precision if applicable
     */
    private Integer precision;

    /**
     * constraints with scale if applicable
     */
    private Integer scale;

    public BigIntegerParam(String name, BigIntegerType type, AccessibleSchema accessibleSchema) {
        super(name, type, accessibleSchema);
    }

    public BigIntegerParam(String name, AccessibleSchema accessibleSchema){
        this(name, new BigIntegerType(), accessibleSchema);
    }

    @Override
    public Object newInstance() throws ClassNotFoundException {
        return getValue();
    }

    @Override
    public NamedTypedValue<BigIntegerType, BigInteger> copyStructure() {
        return new BigIntegerParam(getName(), getType(), accessibleSchema);
    }

    @Override
    public void copyProperties(NamedTypedValue copy) {
        super.copyProperties(copy);
        if (copy instanceof BigIntegerParam){
            ((BigIntegerParam) copy).setMax(max);
            ((BigIntegerParam) copy).setMin(min);
        }
        handleConstraintsInCopy(copy);
    }

    private BigInteger parseValue(String stringValue){
        if (stringValue == null)
            return null;

        return new BigInteger(stringValue);
    }

    @Override
    public void setValueBasedOnDto(ParamDto dto) {
        setValue(parseValue(dto.stringValue));
    }

    @Override
    public void setValueBasedOnInstanceOrJson(Object json) throws JsonProcessingException {
        if (json == null) return;
        setValue(parseValue(json.toString()));
    }

    @Override
    protected void setValueBasedOnValidInstance(Object instance) {
        setValue((BigInteger) instance);
    }

    @Override
    public ParamDto getDto() {
        ParamDto dto = super.getDto();
        handleConstraintsInCopyDto(dto);
        if (getValue() != null)
            dto.stringValue = getValue().toString();
        return dto;
    }

    @Override
    public List<String> newInstanceWithJava(boolean isDeclaration, boolean doesIncludeName, String variableName, int indent) {
        String typeName = getType().getTypeNameForInstance();

        List<String> codes = new ArrayList<>();
        boolean isNull = (getValue() == null);
        String var = CodeJavaGenerator.oneLineInstance(isDeclaration, doesIncludeName, typeName, variableName, null);
        CodeJavaGenerator.addCode(codes, var, indent);
        if (isNull) return codes;

        CodeJavaGenerator.addCode(codes, "{", indent);
        CodeJavaGenerator.addCode(codes, CodeJavaGenerator.setInstance(variableName, CodeJavaGenerator.newObjectConsParams(typeName, getValueAsJavaString())), indent+1);
        CodeJavaGenerator.addCode(codes, "}", indent);

        return codes;
    }

    @Override
    public List<String> newAssertionWithJava(int indent, String responseVarName, int maxAssertionForDataInCollection) {
        // assertion with its string representation
        StringBuilder sb = new StringBuilder();
        sb.append(CodeJavaGenerator.getIndent(indent));
        if (getValue() == null)
            sb.append(CodeJavaGenerator.junitAssertNull(responseVarName));
        else
            sb.append(CodeJavaGenerator.junitAssertEquals(getValueAsJavaString(), responseVarName+".toString()"));

        return Collections.singletonList(sb.toString());
    }

    @Override
    public String getValueAsJavaString() {
        if (getValue() == null)
            return null;
        return "\""+getValue().toString()+"\"";
    }

    @Override
    public BigInteger getMin() {
        return min;
    }

    @Override
    public void setMin(BigInteger min) {
        if (this.min != null && this.min.compareTo(min) >=0)
            return;

        this.min = min;
    }

    @Override
    public BigInteger getMax() {
        return max;
    }

    @Override
    public void setMax(BigInteger max) {
        if (this.max != null && this.max.compareTo(max) <= 0)
            return;
        this.max = max;
    }

    @Override
    public boolean getMinInclusive() {
        return this.minInclusive;
    }

    @Override
    public void setMinInclusive(boolean inclusive) {
        this.minInclusive = inclusive;
    }

    @Override
    public boolean getMaxInclusive() {
        return this.maxInclusive;
    }

    @Override
    public void setMaxInclusive(boolean inclusive) {
        this.maxInclusive = inclusive;
    }

    @Override
    public Integer getPrecision() {
        return precision;
    }

    @Override
    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    @Override
    public Integer getScale() {
        return this.scale;
    }

    @Override
    public void setScale(Integer scale) {
        this.scale = scale;
    }
}
