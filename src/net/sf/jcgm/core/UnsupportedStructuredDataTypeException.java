/*
 * Copyright 2026 Swiss AviationSoftware Ltd.
 */
package net.sf.jcgm.core;

/**
 * Thrown when a structured-data type index is not defined by CGM.
 */
final class UnsupportedStructuredDataTypeException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

	UnsupportedStructuredDataTypeException(int index) {
		super("unknown structured data type index " + index);
	}
}
