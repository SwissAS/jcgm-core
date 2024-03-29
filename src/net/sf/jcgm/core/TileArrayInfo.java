/*
 * <copyright> Copyright 1997-2003 BBNT Solutions, LLC under sponsorship of the
 * Defense Advanced Research Projects Agency (DARPA).
 * Copyright 2009 Swiss AviationSoftware Ltd.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the Cougaar Open Source License as published by DARPA on
 * the Cougaar Open Source Website (www.cougaar.org).
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.sf.jcgm.core;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

/**
 * Contains information to be able to iterate through all tiles or a tile array.
 * 
 * @version $Id: $
 * @author xphc (Philippe Cadé)
 * @since Oct 6, 2010
 */
public class TileArrayInfo {
	private final Point2D.Double startPosition;
	private final int nTilesInPathDirection;
	private final double tileSizeInPathDirection;
	private final double tileSizeInLineDirection;
	private final int nTilesInLineDirection;
	private final int amountOfLastLineCellsInLineDirection;

	/** current index in path direction */
	private int pathIndex = 0;

	/** current index in line direction */
	private int lineIndex = 0;

	/** current position of tile */
	private final Point2D.Double currentPosition;

	/** number of pixels (width) */
	private final int nCellsPerTileInPathDirection;
	/** number of pixels (height) */
	private final int nCellsPerTileInLineDirection;

	TileArrayInfo(Double startPosition, int nTilesInPathDirection,
				  int nCellsPerTileInPathDirection, int nCellsPerTileInLineDirection,
				  double tileSizeInPathDirection, double tileSizeInLineDirection,
				  int nTilesInLineDirection, int amountOfLastLineCellsInLineDirection) {
		this.startPosition = startPosition;
		this.currentPosition = new Point2D.Double(startPosition.x, startPosition.y);
		this.nTilesInPathDirection = nTilesInPathDirection;
		this.nCellsPerTileInPathDirection = nCellsPerTileInPathDirection;
		this.nCellsPerTileInLineDirection = nCellsPerTileInLineDirection;
		this.tileSizeInPathDirection = tileSizeInPathDirection;
		this.tileSizeInLineDirection = tileSizeInLineDirection;
		this.nTilesInLineDirection = nTilesInLineDirection;
		this.amountOfLastLineCellsInLineDirection = amountOfLastLineCellsInLineDirection;
	}

	int getCurrentPathIndex() {
		return this.pathIndex;
	}

	int getCurrentLineIndex() {
		return this.lineIndex;
	}

	/**
	 * @return Tile width in pixels
	 */
	int getNCellsPerTileInPathDirection() {
		return this.nCellsPerTileInPathDirection;
	}

	/**
	 * @return Tile height in pixels
	 */
	int getNCellsPerTileInLineDirection() {
		return this.nCellsPerTileInLineDirection;
	}

	double getTileSizeInPathDirection() {
		return this.tileSizeInPathDirection;
	}

	double getTileSizeInLineDirection() {
		return this.tileSizeInLineDirection;
	}

	Point2D.Double getCurrentTilePosition() {
		return this.currentPosition;
	}

	public int getAmountOfLastLineCellsInLineDirection() {
		return this.amountOfLastLineCellsInLineDirection;
	}

	boolean isLastLine() {
		return this.lineIndex == this.nTilesInLineDirection - 1 ;
	}
	
	/**
	 * This will update the current position for the next tile.
	 */
	void nextTile() {
		// advance on path
		this.pathIndex++;
		if (this.pathIndex == this.nTilesInPathDirection) {
			// must change to next line
			this.pathIndex = 0;
			this.lineIndex++;
		}

		this.currentPosition.x = this.startPosition.x + this.pathIndex * this.tileSizeInPathDirection;
		this.currentPosition.y = this.startPosition.y - this.lineIndex * this.tileSizeInLineDirection;
	}
}