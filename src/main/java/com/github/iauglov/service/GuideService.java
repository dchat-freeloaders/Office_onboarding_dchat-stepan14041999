package com.github.iauglov.service;

import com.github.iauglov.model.NotFoundException;
import com.github.iauglov.persistence.Guide;
import com.github.iauglov.persistence.GuideRepository;
import com.github.iauglov.persistence.UserGuideRepository;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GuideService {

    private final GuideRepository guideRepository;
    private final UserGuideRepository userGuideRepository;

    public void registerNewGuide(String delay, String title, String text) {
        long initDelayInSeconds = Duration.parse("PT" + delay.toUpperCase()).getSeconds();

        Guide guide = new Guide();
        guide.setDelay(initDelayInSeconds);
        guide.setTitle(title);
        guide.setText(text);
        guideRepository.save(guide);
    }

    public void processScheduledGuides() {
//        guideRepository.findAllByStartsWithIsBefore(now()).forEach(guide -> {
//
//        });
    }

    public long getCountOfGuides() {
        return guideRepository.count();
    }

    public List<Guide> getAllGuides() {
        return guideRepository.findAll();
    }

    public void deleteGuide(Integer guideId) throws NotFoundException {
        if (guideRepository.existsById(guideId)) {
            guideRepository.deleteById(guideId);
        } else {
            throw new NotFoundException(String.format("Guide with id '%d' not found", guideId));
        }
    }
}
