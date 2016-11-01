package identify;


import org.activiti.engine.IdentityService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by jin on 2016/11/1.
 */
public class UserAndGroupInUserTaskTest {
    @Rule
    public ActivitiRule rule = new ActivitiRule();

    private IdentityService identityService;

    private RuntimeService runtimeService;

    private TaskService taskService;

    @Test
    public void setup() throws Exception{

        identityService = rule.getIdentityService();
        runtimeService = rule.getRuntimeService();
        taskService = rule.getTaskService();

        User user = identityService.newUser("king");
        user.setFirstName("Jin");
        user.setLastName("Jianjian");
        identityService.saveUser(user);

        User juno = identityService.newUser("juno222");
        juno.setFirstName("L");
        juno.setLastName("Juno");
        identityService.saveUser(juno);



        Group group = identityService.newGroup("weqia");
        group.setName("微洽");
        group.setType("公司");
        identityService.saveGroup(group);

        identityService.createMembership("king","weqia");
        identityService.createMembership("juno","weqia");
    }




    @Test
    @Deployment(resources = {"UserAndGroupInUsertask.bpmn"})
    public void testUserAndGroupInUserTask() {
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("userAndGroupInUserTask");
        assertNotNull(processInstance);

        Task task = taskService.createTaskQuery().taskCandidateOrAssigned("king").singleResult();
        assertNotNull(task);

        taskService.claim(task.getId(),"king");
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().taskCandidateOrAssigned("weqia").singleResult();
        assertNull(task);
    }

    @Test
    @Deployment(resources = {"UserAndGroupInUsertask.bpmn"})
    public void testUserAndGroupInUserTaskWithTwoUser() {
        User juno = identityService.newUser("juno222");
        juno.setFirstName("L");
        juno.setLastName("Juno");
        identityService.saveUser(juno);

        identityService.createMembership("juno","weqia");

        ProcessInstance instance = runtimeService.startProcessInstanceByKey("userAndGroupInUserTask");
        assertNotNull(instance);

        Task kingTask = taskService.createTaskQuery().taskCandidateOrAssigned("king").singleResult();
        assertNotNull(kingTask);

        Task junoTask = taskService.createTaskQuery().taskCandidateOrAssigned("juno").singleResult();
        assertNotNull(junoTask);

        taskService.claim(kingTask.getId(),"king");

        junoTask = taskService.createTaskQuery().taskCandidateOrAssigned("juno").singleResult();
        assertNull(junoTask);

        taskService.complete(kingTask.getId());

        identityService.deleteMembership("juno222","weqia");
        identityService.deleteUser("juno222");

    }






}
