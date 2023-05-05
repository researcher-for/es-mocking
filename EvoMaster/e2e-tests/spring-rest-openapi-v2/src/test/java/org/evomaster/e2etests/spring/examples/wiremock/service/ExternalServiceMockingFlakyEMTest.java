package org.evomaster.e2etests.spring.examples.wiremock.service;

import com.foo.rest.examples.spring.wiremock.service.ServiceController;
import org.evomaster.ci.utils.CIUtils;
import org.evomaster.core.EMConfig;
import org.evomaster.core.problem.rest.HttpVerb;
import org.evomaster.core.problem.rest.RestIndividual;
import org.evomaster.core.problem.rest.resource.RestResourceCalls;
import org.evomaster.core.search.Action;
import org.evomaster.core.search.ActionFilter;
import org.evomaster.core.search.EvaluatedIndividual;
import org.evomaster.core.search.Solution;
import org.evomaster.e2etests.spring.examples.SpringTestBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExternalServiceMockingFlakyEMTest extends SpringTestBase {

    @BeforeAll
    public static void initClass() throws Exception {
        ServiceController serviceController = new ServiceController();
        EMConfig config = new EMConfig();
        config.setInstrumentMR_NET(true);
        SpringTestBase.initClass(serviceController, config);
    }

    @Test
    public void externalServiceMockingTest() throws Throwable {
        runTestHandlingFlakyAndCompilation(
                "ExternalServiceMockingEMGeneratedTest",
                "org.bar.ExternalServiceMockingEMGeneratedTest",
                1000,
                !CIUtils.isRunningGA(),
                (args) -> {

                    // IP set to 127.0.0.5 to confirm the test failure
                    // Use USER for external service IP selection strategy
                    // when running on a personal computer if it's macOS
                    // Note: When running parallel tests it's always good select
                    //  Random as strategy.
                    args.add("--externalServiceIPSelectionStrategy");
                    args.add("USER");
                    args.add("--externalServiceIP");
                    args.add("127.0.0.5");

                    Solution<RestIndividual> solution = initAndRun(args);

                    // The below block of code is an experiment
                    // The value 13 is decided by looking at the generated actions count
                    // manually.
                    List<Action> actions = new ArrayList<>();
                    for (EvaluatedIndividual<RestIndividual> individual : solution.getIndividuals()) {
                        for (RestResourceCalls call : individual.getIndividual().getResourceCalls()) {
                            actions.addAll(call.seeActions(ActionFilter.ONLY_EXTERNAL_SERVICE));
                        }
                    }
                    assertEquals(actions.size(), 13);
                    // End block

                    assertHasAtLeastOne(solution, HttpVerb.GET, 200, "/api/wiremock/external", "true");
                    assertHasAtLeastOne(solution, HttpVerb.GET, 200, "/api/wiremock/external/complex", "true");
                },
                3);
    }
}