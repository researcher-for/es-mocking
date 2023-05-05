package org.evomaster.client.java.controller.api.dto;

public class SutRunDto {

    /**
     * Whether the SUT should be running
     */
    public Boolean run;

    /**
     * Whether the internal state of the SUT should be reset
     */
    public Boolean resetState;

    /**
     * Whether to reset the mocked external services with customized method
     *
     * this depends on the configuration from core side,
     * ie, whether to apply customized method to handle external services
     */
    public Boolean resetMockedExternalServicesWithCustomizedMethod;

    /**
     *  Whether SQL heuristics should be computed.
     *  Note: those can be very expensive
     */
    public Boolean calculateSqlHeuristics;

    /**
     *  Whether SQL execution info should be saved.
     */
    public Boolean extractSqlExecutionInfo;

    /**
     * Specify which categories of Method Replacements to apply when classes are instrumented.
     * Note that this applied once classes are loaded into JVM.
     * Trying to change this after a class has been loaded will have no effect.
     *
     * This is a "," comma separated list of category names.
     */
    public String methodReplacementCategories;


    public SutRunDto() {
    }

    public SutRunDto(Boolean run, Boolean resetState, Boolean resetMockedExternalServicesWithCustomizedMethod, Boolean calculateSqlHeuristics, Boolean extractSqlExecutionInfo, String methodReplacementCategories) {
        if (calculateSqlHeuristics != null && calculateSqlHeuristics && extractSqlExecutionInfo != null && !extractSqlExecutionInfo)
            throw new IllegalArgumentException("extractSqlExecutionInfo should be enabled when calculateSqlHeuristics is enabled");
        this.run = run;
        this.resetState = resetState;
        this.resetMockedExternalServicesWithCustomizedMethod = resetMockedExternalServicesWithCustomizedMethod;
        this.calculateSqlHeuristics = calculateSqlHeuristics;
        this.extractSqlExecutionInfo = extractSqlExecutionInfo;
        this.methodReplacementCategories = methodReplacementCategories;
    }

    public SutRunDto(Boolean run, Boolean resetState, Boolean resetMockedExternalServicesWithCustomizedMethod, Boolean calculateSqlHeuristics, String methodReplacementCategories){
        this(run, resetState, resetMockedExternalServicesWithCustomizedMethod, calculateSqlHeuristics, calculateSqlHeuristics!=null && calculateSqlHeuristics, methodReplacementCategories);
    }
}
