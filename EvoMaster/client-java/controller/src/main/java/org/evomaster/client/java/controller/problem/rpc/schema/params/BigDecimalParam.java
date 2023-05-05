package org.evomaster.client.java.controller.problem.rpc.schema.params;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.evomaster.client.java.controller.api.dto.problem.rpc.ParamDto;
import org.evomaster.client.java.controller.problem.rpc.CodeJavaGenerator;
import org.evomaster.client.java.controller.problem.rpc.schema.types.AccessibleSchema;
import org.evomaster.client.java.controller.problem.rpc.schema.types.BigDecimalType;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.evomaster.client.java.controller.problem.rpc.CodeJavaGenerator.*;

public class BigDecimalParam extends NamedTypedValue<BigDecimalType, BigDecimal> implements NumericConstraintBase<BigDecimal> {

    private BigDecimal min;

    private BigDecimal max;

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

    public BigDecimalParam(String name, BigDecimalType type, AccessibleSchema accessibleSchema) {
        super(name, type, accessibleSchema);
    }

    public BigDecimalParam(String name, AccessibleSchema accessibleSchema){
        this(name, new BigDecimalType(), accessibleSchema);
    }

    @Override
    public Object newInstance() throws ClassNotFoundException {
        return getValue();
    }

    @Override
    public NamedTypedValue<BigDecimalType, BigDecimal> copyStructure() {
        return new BigDecimalParam(getName(), getType(), accessibleSchema);
    }

    @Override
    public void copyProperties(NamedTypedValue copy) {
        super.copyProperties(copy);
        if (copy instanceof BigDecimalParam){
            ((BigDecimalParam) copy).setMax(max);
            ((BigDecimalParam) copy).setMin(min);
        }

        handleConstraintsInCopy(copy);
    }

    @Override
    public void setValueBasedOnDto(ParamDto dto) {
        BigDecimal bd = parseValue(dto.stringValue);
        setValue(bd);
    }

    private BigDecimal parseValue(String stringValue){
        if (stringValue == null)
            return null;

        MathContext mc = null;
        BigDecimal bd = null;
        if (getPrecision() == null)
            bd = new BigDecimal(stringValue);
        else {
            mc = new MathContext(getPrecision());
            bd = new BigDecimal(stringValue, mc);
        }

        if (getScale() != null)
            bd = bd.setScale(getScale(), RoundingMode.HALF_UP);
        return bd;
    }

    @Override
    public void setValueBasedOnInstanceOrJson(Object json) throws JsonProcessingException {
        if (json == null)  return;
        BigDecimal bd = parseValue(json.toString());
        setValue(bd);
    }

    @Override
    protected void setValueBasedOnValidInstance(Object instance) {
        setValue((BigDecimal) instance);
    }

    @Override
    public List<String> newInstanceWithJava(boolean isDeclaration, boolean doesIncludeName, String variableName, int indent) {
        String typeName = getType().getTypeNameForInstance();

        List<String> codes = new ArrayList<>();
        boolean isNull = (getValue() == null);
        String var = oneLineInstance(isDeclaration, doesIncludeName, typeName, variableName, null);
        addCode(codes, var, indent);
        if (isNull) return codes;

        addCode(codes, "{", indent);
        String mcVar = variableName + "_mc";
        String consParam = getValueAsJavaString();
        if (getPrecision() != null){
            addCode(codes, oneLineInstance(true, true, MathContext.class.getName(), mcVar,
                    newObjectConsParams(MathContext.class.getName(), getPrecision().toString())), indent+1);
            consParam += ", "+mcVar;
        }
        addCode(codes, setInstance(variableName, newObjectConsParams(typeName, consParam)), indent+1);
        if (getScale() != null){
            addCode(codes, oneLineSetterInstance("setScale", null, variableName, getScale()+", "+RoundingMode.class.getName()+".HALF_UP"), indent+1);
        }

        addCode(codes, "}", indent);

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
    public ParamDto getDto() {
        ParamDto dto = super.getDto();
        handleConstraintsInCopyDto(dto);
        if (getValue() != null)
            dto.stringValue = getValue().toString();
        return dto;
    }

    @Override
    public String getValueAsJavaString() {
        if (getValue() == null)
            return null;
        return "\""+getValue().toString()+"\"";
    }

    @Override
    public BigDecimal getMin() {
        return min;
    }

    @Override
    public void setMin(BigDecimal min) {
        if (this.min != null && this.min.compareTo(min) >=0)
            return;
        this.min = min;
    }

    @Override
    public BigDecimal getMax() {
        return max;
    }

    @Override
    public void setMax(BigDecimal max) {
        if (this.max != null && this.max.compareTo(max) <= 0)
            return;
        this.max = max;
    }


    @Override
    public boolean getMinInclusive() {
        return minInclusive;
    }

    @Override
    public void setMinInclusive(boolean inclusive) {
        this.minInclusive = inclusive;
    }

    @Override
    public boolean getMaxInclusive() {
        return maxInclusive;
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
