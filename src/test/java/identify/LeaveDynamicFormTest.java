package identify;

import org.activiti.engine.*;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by jin on 2016/11/1.
 */
public class LeaveDynamicFormTest {

    @Rule
    public ActivitiRule rule = new ActivitiRule();
    IdentityService identityService;

    RepositoryService repositoryService;

    FormService formService;
    TaskService taskService;
    HistoryService historyService;
    @Before
    public void before() {

        identityService = rule.getIdentityService();

        repositoryService = rule.getRepositoryService();

        formService = rule.getFormService();

        taskService = rule.getTaskService();

        historyService = rule.getHistoryService();



        User departmentLeader = identityService.newUser("departmentLeader");
        departmentLeader.setFirstName("Department");
        departmentLeader.setLastName("Leader");

        identityService.saveUser(departmentLeader);

        User hr = identityService.newUser("hr");
        hr.setFirstName("H");
        hr.setLastName("R");
        identityService.saveUser(hr);

        Group deptLeader = identityService.newGroup("deptLeader");
        deptLeader.setName("部门管理人员");

        Group hrTeam = identityService.newGroup("hr");
        hrTeam.setName("人事");

        identityService.saveGroup(deptLeader);
        identityService.saveGroup(hrTeam);

        identityService.createMembership("departmentLeader","deptLeader");
        identityService.createMembership("hr","hr");

    }

    @After
    public void after() {
        IdentityService identityService = rule.getIdentityService();

        identityService.deleteMembership("hr","hr");
        identityService.deleteMembership("departmentLeader","deptLeader");
        identityService.deleteUser("departmentLeader");
        identityService.deleteUser("hr");
        identityService.deleteGroup("deptLeader");
        identityService.deleteGroup("hr");
    }
    @Test
    @Deployment(resources = "leaveInAction.bpmn")
    public void allApproved(){


        String currentUserId = "king";
        identityService.setAuthenticatedUserId(currentUserId);

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionKey("leave").singleResult();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Map<String, String> variables = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        String startDate = sdf.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH,2);
        String endDate = sdf.format(calendar.getTime());

        variables.put("startDate", startDate);
        variables.put("endDate", endDate);
        variables.put("reason", "公休");

        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), variables);
        assertNotNull(processInstance);

        Task deptLeaderTask = taskService.createTaskQuery().taskCandidateGroup("deptLeader").singleResult();
        assertNotNull(deptLeaderTask);
        variables = new HashMap<>();

        variables.put("deptLeaderApprove","true");

        formService.submitTaskFormData(deptLeaderTask.getId(), variables);

        Task hrTask = taskService.createTaskQuery().taskCandidateGroup("hr").singleResult();
        assertNotNull(hrTask);

        variables = new HashMap<>();
        variables.put("hrApprove", "true");
        formService.submitTaskFormData(hrTask.getId(),variables);

        Task reportBackTest = taskService.createTaskQuery().taskAssignee("king").singleResult();
        assertNotNull(reportBackTest);

        variables = new HashMap<>();
        variables.put("reportBackDate", sdf.format(calendar.getTime()));
        formService.submitTaskFormData(reportBackTest.getId(),variables);


        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().finished().singleResult();
        assertNotNull(historicProcessInstance);

        Map<String, Object> historyVariables = packageVariables(processInstance);
        assertEquals("ok",historyVariables.get("result"));


    }

    private Map<String, Object> packageVariables(ProcessInstance processInstance) {
        Map<String, Object> historyVariables = new HashMap<>();
        List<HistoricDetail> list = historyService.createHistoricDetailQuery().processInstanceId(processInstance.getId()).list();
        for (HistoricDetail historicDetail : list) {
            if (historicDetail instanceof HistoricFormProperty) {
                HistoricFormProperty field = (HistoricFormProperty) historicDetail;
                historyVariables.put(field.getPropertyId(), field.getPropertyValue());
                System.out.println("form field: taskid= "+field.getTaskId()+","+field.getPropertyId()+"="+field.getPropertyValue());
            } else if (historicDetail instanceof HistoricVariableUpdate) {
                HistoricDetailVariableInstanceUpdateEntity varible = (HistoricDetailVariableInstanceUpdateEntity)historicDetail;
                historyVariables.put(varible.getName(), varible.getValue());
                System.out.println("variable: "+varible.getName() + "=" + varible.getValue());
            }
        }
        return historyVariables;
    }


}
