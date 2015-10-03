package com.shootoff.camera;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.rules.ErrorCollector;
import static org.hamcrest.core.IsEqual.equalTo;

public class ShotDetectionTestor {
	private static int ALLOWED_COORD_VARIANCE = 3;
	
	public void checkShots(ErrorCollector collector, final List<Shot> actualShots, 
			List<Shot> requiredShots, List<Shot> optionalShots, boolean isColorWarning) {
		
		List<Shot> mutableActualShots = new ArrayList<Shot>(actualShots);
		
		for (Shot shot : requiredShots) {
			Optional<Shot> potentialShotMatch = findPotentialShotMatch(mutableActualShots, shot);
			
			collector.checkThat(String.format("Shot (%.2f, %.2f, %s) not found", shot.getX(), shot.getY(),
					shot.getColor().toString()), potentialShotMatch.isPresent(), equalTo(true));
			
			if (potentialShotMatch.isPresent()) {
				if (isColorWarning && !potentialShotMatch.get().getColor().equals(shot.getColor())) {
					System.err.println(String.format("Shot (%.2f, %.2f, %s) detected with wrong color", shot.getX(), shot.getY(),
							shot.getColor().toString()));
				} else {
					collector.checkThat(String.format("Shot (%.2f, %.2f, %s) has wrong color", shot.getX(), shot.getY(),
							shot.getColor().toString()), potentialShotMatch.get().getColor(), equalTo(shot.getColor()));
				}
				
				mutableActualShots.remove(potentialShotMatch.get());
			}
		}
		
		for (Shot shot : optionalShots) {
			Optional<Shot> potentialShotMatch = findPotentialShotMatch(mutableActualShots, shot);
		
			if (potentialShotMatch.isPresent()) {
				if (isColorWarning && !potentialShotMatch.get().getColor().equals(shot.getColor())) {
					System.err.println(String.format("Optional shot (%.2f, %.2f, %s) detected with wrong color", shot.getX(), shot.getY(),
							shot.getColor().toString()));
				} else {
					collector.checkThat(String.format("Optional shot (%.2f, %.2f, %s) has wrong color", shot.getX(), shot.getY(),
							shot.getColor().toString()), potentialShotMatch.get().getColor(), equalTo(shot.getColor()));
				}
				
				mutableActualShots.remove(potentialShotMatch.get());
			} else {
				System.err.println(String.format("Optional shot (%.2f, %.2f, %s) not found", shot.getX(), shot.getY(),
						shot.getColor().toString()));
			}
		}
		
		// If you are getting this failure you're either detecting noise that wasn't detected before
		// or you found a shot that was previously missed and not accounted for, thus you should
		// add it to the required shot list for the respective test.
		collector.checkThat("There are shots that were detected that aren't account for", mutableActualShots.isEmpty(), equalTo(true));
	}
	
	public Optional<Shot> findPotentialShotMatch(List<Shot> actualShots, Shot testedShot) {
		for (Shot shot : actualShots) {
			if (Math.abs(shot.getX() - testedShot.getX()) <= ALLOWED_COORD_VARIANCE &&
				Math.abs(shot.getY() - testedShot.getY()) <= ALLOWED_COORD_VARIANCE) {
				
				return Optional.of(shot);
			}
		}
		
		return Optional.empty();
	}
}
