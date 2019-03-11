package io.onedev.server.web.editable;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.wicket.model.IModel;

import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.ReflectionUtils;
import io.onedev.server.util.OneContext;
import io.onedev.server.web.behavior.PatternSetAssistBehavior;
import io.onedev.server.web.behavior.inputassist.InputAssistBehavior;
import io.onedev.server.web.editable.annotation.PathPatterns;
import io.onedev.server.web.editable.string.StringPropertyEditor;
import io.onedev.server.web.editable.string.StringPropertyViewer;

@SuppressWarnings("serial")
public class PathPatternsEditSupport implements EditSupport {

	@Override
	public PropertyContext<?> getEditContext(PropertyDescriptor descriptor) {
		Method propertyGetter = descriptor.getPropertyGetter();
		PathPatterns pathPatterns = propertyGetter.getAnnotation(PathPatterns.class);
        if (pathPatterns != null) {
        	if (propertyGetter.getReturnType() == String.class) {
        		return new PropertyContext<String>(descriptor) {

    				@Override
    				public PropertyViewer renderForView(String componentId, IModel<String> model) {
    					return new StringPropertyViewer(componentId, descriptor, model.getObject());
    				}

    				@Override
    				public PropertyEditor<String> renderForEdit(String componentId, IModel<String> model) {
    		        	return new StringPropertyEditor(componentId, descriptor, model) {

    						@Override
    						protected InputAssistBehavior getInputAssistBehavior() {
    							return new PatternSetAssistBehavior() {

									@SuppressWarnings("unchecked")
									@Override
									protected List<InputSuggestion> suggest(String matchWith) {
										String suggestionMethod = pathPatterns.value();
										if (suggestionMethod.length() != 0) {
											OneContext.push(new OneContext(getComponent()));
											try {
												return (List<InputSuggestion>) ReflectionUtils.invokeStaticMethod(
														descriptor.getBeanClass(), suggestionMethod, new Object[] {matchWith});
											} finally {
												OneContext.pop();
											}
										} else {
											return Lists.newArrayList();
										}
									}
    								
									@Override
									protected List<String> getHints(TerminalExpect terminalExpect) {
										return Lists.newArrayList(
												"Path containing spaces or starting with dash needs to be quoted",
												"Use * or ? for wildcard match"
												);
									}
									
    							};
    						}
    		        		
    		        	};
    				}
        			
        		};
        	} else {
	    		throw new RuntimeException("Annotation 'PathPatterns' should be applied to property "
	    				+ "of type 'String'");
        	}
        } else {
            return null;
        }
	}

	@Override
	public int getPriority() {
		return DEFAULT_PRIORITY;
	}
	
}
