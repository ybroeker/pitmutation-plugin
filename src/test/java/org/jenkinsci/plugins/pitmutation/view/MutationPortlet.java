package org.jenkinsci.plugins.pitmutation.view;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.plugins.view.dashboard.DashboardPortlet;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Ed Kimber
 */
public class MutationPortlet extends DashboardPortlet {

  @DataBoundConstructor
  public MutationPortlet(String name) {
    super(name);
  }

  @Extension
  public static class DescriptorImpl extends Descriptor<DashboardPortlet> {
    @Override
    public String getDisplayName() {
      return "MutationPortlet";
    }
  }
}
