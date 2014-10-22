package CompilerRuntime;


public class RuleBackgroundElement extends BackgroundElement {
	public static final String RULE_BACKGROUND_NAME = "RULE";
	
	
	@Override
	public Element getNewValue() {
		throw new UnsupportedOperationException("Cannot create new rule.");
	}

	@Override
	protected Element getValue(Element e) {
		return CompilerRuntime.BooleanElement.valueOf(e instanceof Rule);
	}

}
