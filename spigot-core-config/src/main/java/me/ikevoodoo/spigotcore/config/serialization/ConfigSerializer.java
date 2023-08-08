package me.ikevoodoo.spigotcore.config.serialization;

public interface ConfigSerializer<I, O> {

    Class<I> getInputType();
    Class<O> getOutputType();

    O deserialize(I value) throws Throwable;
    I serialize(O value) throws Throwable;


}
