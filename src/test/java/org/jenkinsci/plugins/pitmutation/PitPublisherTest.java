package org.jenkinsci.plugins.pitmutation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.jenkinsci.plugins.pitmutation.PitPublisher.MustImproveCondition;
import org.jenkinsci.plugins.pitmutation.PitPublisher.PercentageThresholdCondition;
import org.jenkinsci.plugins.pitmutation.targets.MutationStats;
import org.jenkinsci.plugins.pitmutation.targets.ProjectMutations;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import hudson.model.AbstractBuild;
import hudson.model.Result;

/**
 * User: Ed Kimber Date: 17/03/13 Time: 17:55
 */
public class PitPublisherTest {
    private @Mock PitBuildAction action_;
    private @Mock AbstractBuild owner_;
    private @Mock ProjectMutations report_;
    private @Mock MutationStats stats_;
    
    class PitPublisherTSS extends PitPublisher {
        boolean infoImproveLogged = false;
        boolean infoPercentLogged = false;

        public PitPublisherTSS(String mutationStatsFile, float minimumKillRatio, boolean killRatioMustImprove) {
            super(mutationStatsFile, minimumKillRatio, killRatioMustImprove);
        }

        class MustImproveConditionTSS extends MustImproveCondition {
             void logInfo(final PitBuildAction action, MutationStats stats) {
                 infoImproveLogged = true;
             }
        }
        
        Condition mustImprove() {
            return new MustImproveConditionTSS();
        }
        
        class PercentageThresholdConditionTSS extends PercentageThresholdCondition {
            PercentageThresholdConditionTSS(float percentage) {
                super(percentage);
            }
            void dologging(MutationStats stats) {
                infoPercentLogged = true;
            }            
        }
        
        Condition percentageThreshold(final float percentage) {
            return new PercentageThresholdConditionTSS(percentage);
        }        
    }

    private PitPublisher publisher_;
    private PitPublisherTSS publisherTSS_;
    @Before
    public void setup() {
        action_ = Mockito.mock(PitBuildAction.class);
        owner_ = Mockito.mock(AbstractBuild.class);
        report_ = Mockito.mock(ProjectMutations.class);
        stats_ = Mockito.mock(MutationStats.class);
    }
    
    private float minimumKillRatio_ = 0.25f;

    @Test
    public void testDecideBuildResultFailsIfKillRatioTooLow() {
        publisherTSS_ = createPitPublisher(false);
        publisher_ = publisherTSS_;
        final float TOO_LOW_RATIO =   0.2f;
        setupBoilderPlateMocks();
        Mockito.when(stats_.getKillPercent()).thenReturn(TOO_LOW_RATIO);        
        assertEquals(Result.FAILURE, publisher_.decideBuildResult(action_));
        assertFalse(publisherTSS_.infoImproveLogged);
        assertTrue(publisherTSS_.infoPercentLogged);
    }
    
    @Test
    public void testDecideBuildResultSuccessIfKillRatioSame() {
        publisher_ = createPitPublisher(false);
        final float OK_RATIO =   minimumKillRatio_;
        final float BIGGER_RATIO =   minimumKillRatio_ + 0.2f;
        setupBoilderPlateMocks();
        Mockito.when(stats_.getKillPercent()).thenReturn(OK_RATIO, BIGGER_RATIO);        
        assertEquals(Result.SUCCESS, publisher_.decideBuildResult(action_));
        assertEquals(Result.SUCCESS, publisher_.decideBuildResult(action_));
    }
    
    @Test
    public void testMustImproveConditionSucceedsWhenPreviousActionIsNull() {
        publisher_ = createPitPublisher(true);
        final float BIGGER_RATIO =   minimumKillRatio_ + 0.2f;
        setupBoilderPlateMocks();
        Mockito.when(stats_.getKillPercent()).thenReturn( BIGGER_RATIO);        
        assertEquals(Result.SUCCESS, publisher_.decideBuildResult(action_));
    }
    
    @Test
    public void testMustImproveConditionFailsWhenWhenPreviousActionKillRatioIsBetter() {
        publisher_ = createPitPublisher(true);
        setupBoilderPlateMocks();
        setupPercentKillRatioOK();        
        PitBuildAction prevAction = mock(PitBuildAction.class);
        ProjectMutations prevReport_ = Mockito.mock(ProjectMutations.class);
        MutationStats prevStats_ = Mockito.mock(MutationStats.class);
        when(action_.getPreviousAction()).thenReturn( prevAction);    
        when(prevAction.getReport()).thenReturn(prevReport_);
        when(prevReport_.getMutationStats()).thenReturn(prevStats_);
        when(stats_.getKillPercent()).thenReturn( minimumKillRatio_ + 0.5f);
        when(prevStats_.getKillPercent()).thenReturn( minimumKillRatio_ + 0.6f, // previous is better
                          minimumKillRatio_ + 0.5f, // equal
                          minimumKillRatio_ + 0.4f); //previous is worse
        
        assertEquals(Result.UNSTABLE, publisher_.decideBuildResult(action_)); //previous is better
        assertEquals(Result.SUCCESS, publisher_.decideBuildResult(action_)); // previous is equal
        assertEquals(Result.SUCCESS, publisher_.decideBuildResult(action_)); //previous is worse
    }

    private void setupPercentKillRatioOK() {
        final float BIGGER_RATIO =   minimumKillRatio_ + 0.2f;
        when(stats_.getKillPercent()).thenReturn( BIGGER_RATIO);
    }

    private PitPublisherTSS createPitPublisher(boolean mustImprove) {
        return new PitPublisherTSS("**/mutations.xml", minimumKillRatio_, mustImprove);
    }

    private void setupBoilderPlateMocks() {
        Mockito.when(action_.getReport()).thenReturn(report_);
        Mockito.when(report_.getMutationStats()).thenReturn(stats_);
    }

}
