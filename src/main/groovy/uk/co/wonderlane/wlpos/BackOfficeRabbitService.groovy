package uk.co.wonderlane.wlpos

public class BackOfficeRabbitService extends RabbitService {

    public BackOfficeRabbitService(String host, int port, String username, String password) {
        super(host, port, username, password, null, null);
    }

    @Override
    public void init() {
        super.init();
    }
}