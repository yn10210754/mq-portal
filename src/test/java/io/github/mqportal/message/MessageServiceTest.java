package io.github.mqportal.message;

import io.github.mqportal.cluster.service.MqAdminService;
import io.github.mqportal.message.dto.ResetOffsetRequest;
import io.github.mqportal.message.service.MessageService;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MessageServiceTest {

    @Test
    void resetOffsetPassesArgumentsToAdmin() throws Exception {
        DefaultMQAdminExt admin = mock(DefaultMQAdminExt.class);
        MessageService service = new MessageService(new MqAdminService(admin));

        ResetOffsetRequest request = new ResetOffsetRequest();
        request.setTopic("t");
        request.setGroup("g");
        request.setTimestamp(123456789L);
        request.setForce(true);

        service.resetOffset(request);

        verify(admin).resetOffsetByTimestamp("t", "g", 123456789L, true);
    }
}
