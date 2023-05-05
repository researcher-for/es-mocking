package org.evomaster.client.java.controller.api.dto.problem;

import org.evomaster.client.java.controller.api.dto.problem.rpc.RPCActionDto;
import org.evomaster.client.java.controller.api.dto.problem.rpc.RPCInterfaceSchemaDto;
import org.evomaster.client.java.controller.api.dto.problem.rpc.SeededRPCTestDto;

import java.util.List;

/**
 * a dto to collect info of RPC problem to be tested
 * that is sent to core
 */
public class RPCProblemDto extends ProblemInfoDto{
    /**
     * a list of accessible endpoints grouped by interface
     */
    public List<RPCInterfaceSchemaDto> schemas;


    /**
     * a list of actions with specified value that are used for handling auth of the SUT
     * the action refers to a method at SutController
     */
    public List<RPCActionDto> localAuthEndpoints;

    /**
     * a list of reference of the endpoints, and
     * the reference is identified based on the index where to be specified in the driver
     */
    public List<Integer> localAuthEndpointReferences;

    /**
     * a list of seeded RPC tests
     */
    public List<List<RPCActionDto>> seededTestDtos;

}
