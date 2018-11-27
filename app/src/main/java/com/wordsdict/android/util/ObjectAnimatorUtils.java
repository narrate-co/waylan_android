package com.wordsdict.android.util;


import android.animation.ObjectAnimator;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Property;

public class ObjectAnimatorUtils {

    public static <T> ObjectAnimator ofPointF(T target, Property<T, PointF> property, Path path) {
        return ObjectAnimator.ofObject(target, property, null, path);
    }

    public static <T> ObjectAnimator ofIntProp(T target, Property<T, Integer> property, Integer... ints) {
        return ObjectAnimator.ofObject(target, property, null, ints);
    }

    private ObjectAnimatorUtils() {
    }
}
