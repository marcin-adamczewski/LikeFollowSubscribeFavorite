package com.appunite.likefollowsubscribe;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;


public class Cache<K, V> {

    @Nonnull
    private final CacheProvider<K, V> provider;
    @Nonnull
    private final Map<K, V> cached = new HashMap<>();

    public interface CacheProvider<K, V> {

        @Nonnull
        V load(@Nonnull K key);
    }

    public Cache(@Nonnull CacheProvider<K, V> provider) {
        this.provider = provider;
    }

    @Nonnull
    public V get(@Nonnull K key) {
        synchronized (cached) {
            final V value = cached.get(key);
            if (value != null) {
                return value;
            }
            final V newValue = provider.load(key);
            cached.put(key, newValue);
            return newValue;
        }
    }

    public void invalidate() {
        cached.clear();
    }
}
