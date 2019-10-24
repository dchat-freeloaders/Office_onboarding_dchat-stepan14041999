package com.github.iauglov.service;

import com.github.iauglov.model.GuideCreatingStage;
import com.github.iauglov.persistence.Guide;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CrudCache {

    public final Map<Integer, GuideCreatingStage> guideCreatingMap = new HashMap<>();
    public final Map<Integer, Integer> guideEditingMap = new HashMap<>();
    public final Set<Integer> guideTitleEditing = new HashSet<>();
    public final Set<Integer> guideTextEditing = new HashSet<>();
    public final Set<Integer> guideDelayEditing = new HashSet<>();
    public final Map<Integer, Guide> guideMap = new HashMap<>();

    public final Set<Integer> questionCreating = new HashSet<>();
    public final Map<Integer, Integer> questionEditingMap = new HashMap<>();
    public final Map<Integer, Integer> questionCachedToLink = new HashMap<>();

    public final Set<Integer> answerCreating = new HashSet<>();
    public final Map<Integer, Integer> answerEditingMap = new HashMap<>();
    public final Map<Integer, Integer> answerCachedToLink = new HashMap<>();
}
