/*
 * Copyright 2026 Swiss AviationSoftware Ltd.
 */
package net.sf.jcgm.core;

/**
 * Thrown when a CGM class or element ID is outside the implemented enum range.
 */
public class UnsupportedElementException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public UnsupportedElementException(int value) {
		super("Unsupported CGM value: " + value);
	}
}
