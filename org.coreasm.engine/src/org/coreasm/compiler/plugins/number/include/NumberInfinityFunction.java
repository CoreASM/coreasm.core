package org.coreasm.compiler.plugins.number.include;

import java.util.List;

import CompilerRuntime.Element;
import CompilerRuntime.FunctionElement;

public class NumberInfinityFunction extends FunctionElement {

    public NumberInfinityFunction() {
        setFClass(FunctionClass.fcDerived);
    }
	@Override
	public Element getValue(List<? extends Element> args) {
		return NumberElement.POSITIVE_INFINITY;
	}

}
