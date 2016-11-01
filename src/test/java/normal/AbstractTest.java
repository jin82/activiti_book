package normal;

import org.activiti.engine.IdentityService;
import org.activiti.engine.impl.test.AbstractActivitiTestCase;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;

/**
 * Created by jin on 2016/11/1.
 */
public class AbstractTest extends AbstractActivitiTestCase {

    @Rule
    public ActivitiRule rule = new ActivitiRule();

    @Override
    protected void initializeProcessEngine() {
        IdentityService service = rule.getIdentityService();
        processEngine = rule.getProcessEngine();
    }
}
