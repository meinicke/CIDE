package de.ovgu.cide.importantenna;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import cide.gast.IASTNode;
import cide.gast.ISourceFile;
import cide.gparser.ParseException;
import de.ovgu.cide.editor.SelectionFinder;
import de.ovgu.cide.features.FeatureModelNotFoundException;
import de.ovgu.cide.features.IFeature;
import de.ovgu.cide.features.IFeatureModel;
import de.ovgu.cide.features.source.ColoredSourceFile;
import de.ovgu.cide.features.source.SourceFileColorManager;
import de.ovgu.cide.utils.StrUtils;

public class CIDEAnnotationParser implements IResourceVisitor {

	class AnnotationMarker {

		private int startPosition;
		private int endPosition;
		String featureName;

		public AnnotationMarker(int startPosition, String featureName) {
			this.startPosition = startPosition;
			this.featureName = featureName;
			endPosition = -1;
		}

		@Override
		public String toString() {
			return startPosition + "-" + endPosition + ": " + featureName;
		}

		public Set<IFeature> getColors(IFeatureModel featureModel) {
			Set<IFeature> features = featureModel.getFeatures();
			Set<IFeature> result = new HashSet<IFeature>();

			for (IFeature feature : features) {
				if (feature.getName().equals(featureName))
					result.add(feature);
			}

			assert !features.isEmpty() : "No feature found, should be "
					+ featureName;

			return result;
		}
	}

	private IProgressMonitor monitor;

	public CIDEAnnotationParser(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public boolean visit(IResource resource) throws CoreException {
		System.out.println(resource.getName());
		if (resource instanceof IFile) {
			if (monitor != null)
				monitor.subTask("Importing annotations: " + resource.getName());
			try {
				ColoredSourceFile csFile = ColoredSourceFile
						.getColoredSourceFile((IFile) resource);

				if (csFile.isColored())
					loadAnnotations(csFile);

			} catch (FeatureModelNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	private void loadAnnotations(final ColoredSourceFile csFile)
			throws CoreException {
		System.out.println(csFile);

		try {
			String content = StrUtils.strFromInputStream(csFile.getResource()
					.getContents(true));

			final List<AnnotationMarker> annotations = findAnnotations(content);

			final SourceFileColorManager cm = csFile.getColorManager();
			cm.beginBatch();
			ISourceFile ast = csFile.getAST();

			for (AnnotationMarker annotation : annotations) {

				List<IASTNode> nodes = new ArrayList<IASTNode>();
				ast
						.accept(new SelectionFinder(nodes,
								annotation.startPosition,
								annotation.endPosition
										- annotation.startPosition, true));

				Set<IFeature> colors = annotation.getColors(csFile
						.getFeatureModel());
				for (IASTNode node : nodes)
					cm.setColors(node, colors);
			}

			cm.endBatch();

			// String newContent = stripTextualAnnotations(content);
			//
			// newContent = prettyPrint(newContent);
			//
			// csFile.getResource().setContents(
			// StrUtils.strToInputStream(newContent), true, true, null);

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String prettyPrint(String content) {
		// CodeFormatter formatter = ToolFactory.createCodeFormatter(JavaCore
		// .getOptions(), ToolFactory.M_FORMAT_EXISTING);
		//
		// TextEdit edit = formatter.format(CodeFormatter.K_COMPILATION_UNIT,
		// content, 0, content.length(), 0, null);
		// IDocument document = new Document(content);
		// try {
		// if (edit != null)
		// edit.apply(document);
		// } catch (MalformedTreeException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (BadLocationException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// return document.get();
		return content;
	}

	private String stripTextualAnnotations(String content) {
		return content;// .replaceAll("/\\*IF\\[.*?\\]\\*/", "").replaceAll(
		// "/\\*ENDIF\\[.*?\\]\\*/", "");
	}

	List<AnnotationMarker> findAnnotations(String content) {
		Stack<AnnotationMarker> annotationStack = new Stack<AnnotationMarker>();
		final List<AnnotationMarker> annotations = new LinkedList<AnnotationMarker>();
		int offset = 0;
		int ifdefOffset = content.indexOf("#ifdef");
		int endifOffset = content.indexOf("#endif");
		try {
			while (ifdefOffset >= 0 || endifOffset >= 0) {
				if (ifdefOffset >= 0 && ifdefOffset < endifOffset) {
					annotationStack.push(new AnnotationMarker(ifdefOffset
							+ offset, parseFeatures(content, ifdefOffset)));
					content = content.substring(ifdefOffset + 1);
					offset += ifdefOffset + 1;
				}
				if (ifdefOffset < 0 || endifOffset < ifdefOffset) {
					if (!annotationStack.isEmpty()) {
						AnnotationMarker start = annotationStack.pop();
						start.endPosition = findEnd(content, endifOffset)
								+ offset;
						annotations.add(0, start);
					}
					content = content.substring(endifOffset + 1);
					offset += endifOffset + 1;
				}
				ifdefOffset = content.indexOf("#ifdef");
				endifOffset = content.indexOf("#endif");
			}
		} catch (EmptyStackException e) {
			System.err.println("illformed annotations");
		}
		System.out.println(annotations);
		return annotations;
	}

	private int findEnd(String content, int endifOffset) {
		return endifOffset + content.substring(endifOffset).indexOf("]*/") + 4;
	}

	String parseFeatures(String content, int ifdefOffset) {
		content = content.substring(ifdefOffset + 6);
		content = content.substring(1, content.indexOf('\n'));

		return content.trim();
	}

}