package org.jenkinsci.plugins.pitmutation.targets;

import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import org.jenkinsci.plugins.pitmutation.Mutation;
import org.jenkinsci.plugins.pitmutation.MutationReport;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author edward
 */
public class ModuleResult extends MutationResult<ModuleResult> {

  public ModuleResult(String name, MutationResult parent, MutationReport report) {
    super(name, parent);
    name_ = name;
    report_ = report;
  }

  public String getDisplayName() {
    return "Module: " + getName();
  }

  @Override
  public MutationStats getMutationStats() {
    return report_.getMutationStats();
  }

  public Map<String, MutatedPackage> getChildMap() {
    return Maps.transformEntries(report_.getMutationsByPackage().asMap(), packageTransformer_);
  }

//  public Collection<MutationStats> getStatsForNewTargets() {
//    return Maps.transformEntries(
//            Maps.difference(
//                    reports_.getFirst().getMutationsByClass().asMap(),
//                    reports_.getSecond().getMutationsByClass().asMap())
//                    .entriesOnlyOnLeft(),
//            statsTransformer_).values();
//  }

//  public Collection<Pair<MutatedClass>> getClassesWithNewSurvivors() {
//    return Maps.transformEntries(mutationDifference_, classMutationDifferenceTransform_).values();
//  }

  public String getName() {
    return name_;
  }

  private Maps.EntryTransformer<String, Collection<Mutation>, MutatedPackage> packageTransformer_ =
    new Maps.EntryTransformer<String, Collection<Mutation>, MutatedPackage>() {
      public MutatedPackage transformEntry(String name, Collection<Mutation> mutations) {
        logger.log(Level.FINER, "found " + report_.getMutationsForPackage(name).size() + " reports for " + name);
        return new MutatedPackage(name, ModuleResult.this, Multimaps.index(report_.getMutationsForPackage(name), MutationReport.classIndexFunction));
      }
    };


  private static final Maps.EntryTransformer<String, Collection<Mutation>, MutationStats> statsTransformer_ =
    new Maps.EntryTransformer<String, Collection<Mutation>, MutationStats>() {
      public MutationStats transformEntry(String name, Collection<Mutation> mutations) {
        return new MutationStatsImpl(name, mutations);
      }
    };


//  private Maps.EntryTransformer<String, MapDifference.ValueDifference<Collection<Mutation>>, Pair<MutatedClass>> classMutationDifferenceTransform_ =
//          new Maps.EntryTransformer<String, MapDifference.ValueDifference<Collection<Mutation>>, Pair<MutatedClass>>() {
//            public Pair<MutatedClass> transformEntry(String name, MapDifference.ValueDifference<Collection<Mutation>> value) {
////              return MutatedClass.createPair(name, getOwner(), value.leftValue(), value.rightValue());
//            }
//          };

  @Override
  public int compareTo(@Nonnull ModuleResult other) {
    return this.getMutationStats().getUndetected() - other.getMutationStats().getUndetected();
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof ModuleResult
      && Objects.equals(this.getMutationStats(), ((ModuleResult) other).getMutationStats())
      && Objects.equals(this.getChildMap(), ((ModuleResult) other).getChildMap())
      && Objects.equals(this.getDisplayName(), ((ModuleResult) other).getDisplayName())
      && Objects.equals(this.getUrl(), ((ModuleResult) other).getUrl())
      && Objects.equals(this.getSourceFileContent(), ((ModuleResult) other).getSourceFileContent());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getMutationStats(), this.getChildMap(), this.getDisplayName(),
      this.getUrl(), this.getSourceFileContent());
  }

  private static final Logger logger = Logger.getLogger(ModuleResult.class.getName());

  private Map<String, MapDifference.ValueDifference<Collection<Mutation>>> mutationDifference_;
  private MutationReport report_;
  private String name_;
}
