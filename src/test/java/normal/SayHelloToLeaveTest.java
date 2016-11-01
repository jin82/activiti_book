package normal;

import org.activiti.engine.*;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by jin on 2016/10/31.
 */
public class SayHelloToLeaveTest {

    @Test
    public void testStartProcess() throws Exception {
        ProcessEngine processEngine = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration().buildProcessEngine();
        RepositoryService repositoryService = processEngine.getRepositoryService();

        repositoryService.createDeployment()
                .addClasspathResource("sayHelloToLeave.bpmn")
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

        assertEquals("SayHelloToLeave", processDefinition.getKey());

        RuntimeService runtimeService = processEngine.getRuntimeService();
        Map<String,Object> variables = new HashMap<>();
        variables.put("applyUser", "employee1");
        variables.put("days", 3);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("SayHelloToLeave", variables);

        assertNotNull(processInstance);

        System.out.println("pid=" + processInstance.getId() + ", pdid= " +processInstance.getProcessDefinitionId());

        TaskService taskService = processEngine.getTaskService();
        Task taskOfDeptLeader = taskService.createTaskQuery()
                .taskCandidateGroup("deptLeader").singleResult();
        assertNotNull(taskOfDeptLeader);

        assertEquals("领导审批",taskOfDeptLeader.getName());

        taskService.claim(taskOfDeptLeader.getId(),"leaderUser");

        variables = new HashMap<>();
        variables.put("approved", true);

        taskService.complete(taskOfDeptLeader.getId(),variables);
        taskOfDeptLeader = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();

        assertNull(taskOfDeptLeader);

        HistoryService historyService = processEngine.getHistoryService();
        long count = historyService.createHistoricProcessInstanceQuery().finished().count();
        assertEquals(1,count);


    }

}
