package gg.tlr.jarcon.core;

public class MetaHandler extends AbstractEventHandler<AbstractEventHandler<?>> {
    @Override
    protected void handle(Packet packet) {
        dispatch(eventHandler -> eventHandler.handle(packet));
    }

    @Override
    protected void handle(JarconClient.State previous, JarconClient.State current) {
        dispatch(listeners -> listeners.handle(previous, current));
    }

    @Override
    public void shutdown() {
        dispatch(AbstractEventHandler::shutdown);
        super.shutdown();
    }
}
