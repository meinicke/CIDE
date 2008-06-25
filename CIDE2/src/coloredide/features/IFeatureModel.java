package coloredide.features;

import java.util.Set;

import org.eclipse.core.resources.IProject;

import coloredide.configuration.AbstractConfigurationPage;

public interface IFeatureModel {
	/**
	 * returns the project this feature model provider belongs to
	 */
	IProject getProject();

	// /**
	// * add and remove listeners so that changes to the feature model are
	// * recognized. this listener is internally propagated to the
	// * IColorChangeListener, so there is usually no need to install an own
	// * listener here.
	// */
	// void addListener(IFeatureModelChangeListener listener);
	//
	// /**
	// * removes listener,
	// *
	// * @see IFeatureModel.addListener
	// */
	// void removeListener(IFeatureModelChangeListener listener);

	/**
	 * returns all features of this feature model
	 * 
	 * @return unmodifiable collection of features (not sorted, no duplicates)
	 */
	Set<IFeature> getFeatures();

	/**
	 * returns the subset of features that is currently marked visible
	 * 
	 * @return unmodifiable collection of visible features (not sorted, no
	 *         duplicates)
	 */
	Set<IFeature> getVisibleFeatures();

	/**
	 * returns a specific feature by a given ID which identifies this feature,
	 * or null if no such feature exists
	 * 
	 * @see IFeature.getId
	 * @param id
	 *            unique featureid in this project
	 * @return the feature or null if no such feature exists
	 */
	IFeature getFeature(long id);

	/**
	 * Page to select a valid configuration. If not specified a configuration is
	 * selected without validation from all (visible and invisible) features.
	 * 
	 * creates a wizard page to select a configuration. this configuration is
	 * validated against this feature model if possible and allows only to
	 * select valid configurations
	 * 
	 * the default implementation NonValidatingConfigurationListPage can be
	 * returned by default
	 * 
	 * @return page
	 */
	AbstractConfigurationPage getConfigurationPage(String pageName);

}