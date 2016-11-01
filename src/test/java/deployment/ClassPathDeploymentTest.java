package deployment;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jin on 2016/11/1.
 */
public class ClassPathDeploymentTest {

    @Rule
    public ActivitiRule rule = new ActivitiRule();

    @Test
    public void test() {
        RepositoryService repositoryService = rule.getRepositoryService();
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        deploymentBuilder.addClasspathResource("UserAndGroupInUsertask.bpmn");
        deploymentBuilder.deploy();

        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        long count = query.processDefinitionKey("userAndGroupInUserTask").count();
        assertEquals(count, 1);



    }

}
