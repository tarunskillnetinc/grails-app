package uk.co.wonderlane.wlpos

public class BackOfficeRabbitService extends RabbitService {

    public BackOfficeRabbitService(String host, int port, String username, String password) {
        super(host, port, username, password, null, null, new BackOfficeLogger()) // TODO Implement an actual BackOfficeLogger?
    }

    @Override
    public void init() {
        super.init();
    }
}