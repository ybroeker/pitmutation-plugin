package org.jenkinsci.plugins.pitmutation.targets;

import com.google.common.collect.Maps;
import hudson.model.Run;
import org.jenkinsci.plugins.pitmutation.Mutation;
import org.jenkinsci.plugins.pitmutation.MutationReport;
import org.jenkinsci.plugins.pitmutation.PitBuildAction;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author Ed Kimber
 */
public class ProjectMutations extends MutationResult<ProjectMutations> {
  public ProjectMutations(PitBuildAction action) {
    super("aggregate", null);
    action_ = action;
  }

  @Override
  public Run<?, ?> getOwner() {
    return action_.getOwner();
  }

  public ProjectMutations getPreviousResult() {
    return action_.getPreviousAction().getReport();
  }

  @Override
  public MutationStats getMutationStats() {
    return aggregateStats(action_.getReports().values());
  }

  private static MutationStats aggregateStats(Collection<MutationReport> reports) {
    MutationStats stats = new MutationStatsImpl("", new ArrayList<Mutation>(0));
    for (MutationReport report : reports) {
      stats = stats.aggregate(report.getMutationStats());
    }
    return stats;
  }

  @Override
  public String getName() {
    return "Aggregated Reports";
  }

  public String getDisplayName() {
    return "Modules";
  }

  public Map<String, ? extends MutationResult<?>> getChildMap() {
    return Maps.transformEntries(action_.getReports(), moduleTransformer_);
  }

  private Maps.EntryTransformer<String, MutationReport, ModuleResult> moduleTransformer_ =
    new Maps.EntryTransformer<String, MutationReport, ModuleResult>() {
      public ModuleResult transformEntry(String moduleName, MutationReport report) {
        return new ModuleResult(moduleName, ProjectMutations.this, report);
      }
    };

  @Override
  public int compareTo(@Nonnull ProjectMutations other) {
    return this.getMutationStats().getUndetected() - other.getMutationStats().getUndetected();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof ProjectMutations
      && Objects.equals(this.getMutationStats(), ((ProjectMutations) other).getMutationStats())
      && Objects.equals(this.getChildMap(), ((ProjectMutations) other).getChildMap())
      && Objects.equals(this.getDisplayName(), ((ProjectMutations) other).getDisplayName())
      && Objects.equals(this.getUrl(), ((ProjectMutations) other).getUrl())
      && Objects.equals(this.getSourceFileContent(), ((ProjectMutations) other).getSourceFileContent())
      && Objects.equals(this.getOwner(), ((ProjectMutations) other).getOwner())
      && Objects.equals(this.getPreviousResult(), ((ProjectMutations) other).getPreviousResult());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getMutationStats(), this.getChildMap(), this.getDisplayName(),
      this.getUrl(), this.getSourceFileContent(), this.getOwner(), this.getPreviousResult());
  }

  private PitBuildAction action_;
}
