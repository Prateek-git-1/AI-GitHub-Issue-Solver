package com.prateek.ai.aigithubissuesolver.agent;

import com.prateek.ai.aigithubissuesolver.state.AgentState;

public interface BaseAgent {

    /**
     * Every agent gets the shared state,
     * does its job, and updates the state.
     *
     * @param state - shared pipeline state
     */
    void run(AgentState state);

    /**
     * Returns agent name — used in logs
     */
    String getAgentName();

}
