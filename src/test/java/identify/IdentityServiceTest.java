package identify;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by jin on 2016/11/1.
 */

public class IdentityServiceTest {



    @Rule
    public ActivitiRule rule = new ActivitiRule();

    @Test
    public void testUser() {
        IdentityService identityService = rule.getIdentityService();

        User user = identityService.newUser("king");
        user.setFirstName("Jin");
        user.setLastName("Jianjian");
        user.setEmail("jin820100449@qq.com");

        identityService.saveUser(user);

        assertNotNull(identityService.createUserQuery().userId("king").singleResult());

        identityService.deleteUser("king");

        assertNull(identityService.createUserQuery().userId("king").singleResult());
        assertEquals(identityService.createUserQuery().count(),0);


    }

    @Test
    public void testGroup() {
        IdentityService identityService = rule.getIdentityService();
        Group group = identityService.newGroup("developer");
        group.setName("微洽");
        group.setType("weqiaer");

        identityService.saveGroup(group);

        List<Group> groups = identityService.createGroupQuery().groupId("developer").list();
        assertNotNull(groups);
        assertEquals(1,groups.size());

        identityService.deleteGroup("developer");

        assertNull(identityService.createGroupQuery().groupId("developer").singleResult());

        assertEquals(0,identityService.createGroupQuery().count());
    }


    @Test
    public void testUserAndGroupMemberShip() {
        IdentityService service = rule.getIdentityService();

        User user = service.newUser("king");
        user.setFirstName("Jin");
        user.setLastName("Jianjian");
        user.setEmail("jin820100449@qq.com");
        service.saveUser(user);

        Group group = service.newGroup("weqia");
        group.setName("微洽");
        group.setType("天堂软件园");
        service.saveGroup(group);

        service.createMembership("king","weqia");

        User groupUser = service.createUserQuery().memberOfGroup("weqia").singleResult();
        assertNotNull(groupUser);
        assertEquals(groupUser.getId(),"king");

        Group userGroup = service.createGroupQuery().groupMember("king").singleResult();
        assertNotNull(userGroup);
        assertEquals("weqia",userGroup.getId());

        service.deleteGroup("weqia");
        service.deleteUser("king");

        groupUser = service.createUserQuery().memberOfGroup("weqia").singleResult();
        assertNull(groupUser);

        userGroup = service.createGroupQuery().groupMember("king").singleResult();
        assertNull(userGroup);
    }

}
