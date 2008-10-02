package de.ovgu.cide.fm.purevariants;

import java.util.Collections;
import java.util.Set;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import coloredide.configuration.AbstractConfigurationPage;
import coloredide.features.IFeature;
import coloredide.features.IFeatureModel;

public class NoConfigurationPage extends AbstractConfigurationPage {

	public NoConfigurationPage(String pageName, IFeatureModel featureModel) {
		super(pageName, featureModel);
		this
				.setErrorMessage("Not available. Use pure::variant transformation mechanism instead.");
	}

	@Override
	protected Control createMainControl(Composite composite) {
		return composite;
	}

	@Override
	public Set<IFeature> getNotSelectedFeatures() {
		return Collections.EMPTY_SET;
	}

	@Override
	public Set<IFeature> getSelectedFeatures() {
		return Collections.EMPTY_SET;
	}

}
