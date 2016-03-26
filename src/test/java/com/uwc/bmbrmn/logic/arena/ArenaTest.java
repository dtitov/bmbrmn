package com.uwc.bmbrmn.logic.arena;

import com.uwc.bmbrmn.BmbrmnApplication;
import com.uwc.bmbrmn.model.tiles.Cell;
import com.uwc.bmbrmn.model.tiles.impl.Space;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.xguzm.pathfinding.grid.GridCell;

import java.math.BigInteger;

import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = BmbrmnApplication.class)
@WebAppConfiguration
public class ArenaTest {

    @Autowired
    private Arena arena;

    @Value("${arena.width}")
    private int width;

    @Value("${arena.height}")
    private int height;

    @Test
    public void getWidth() throws Exception {
        Assert.assertEquals(width, arena.getWidth());
    }

    @Test
    public void getHeight() throws Exception {
        Assert.assertEquals(height, arena.getHeight());
    }

    @Test
    public void getPlayer() throws Exception {
        Assert.assertNotNull(arena.getPlayer());
    }

    @Test
    public void getBots() throws Exception {
        Assert.assertThat(arena.getBots(), is(not(empty())));
    }

    @Test
    public void getTimeInSeconds() throws Exception {
        Assert.assertThat(arena.getTimeInSeconds(), is(greaterThan(BigInteger.ZERO.intValue())));
    }

    @Test
    public void getCellAt() throws Exception {
        Assert.assertNotNull(arena.getCellAt(BigInteger.ZERO.intValue(), BigInteger.ZERO.intValue()));
    }

    @Test
    public void moveItem() throws Exception {
        Cell zeroCell = arena.getCellAt(BigInteger.ZERO.intValue(), BigInteger.ZERO.intValue());
        arena.moveItem(zeroCell, BigInteger.ONE.intValue(), BigInteger.ZERO.intValue());
        Assert.assertEquals(zeroCell, arena.getCellAt(BigInteger.ONE.intValue(), BigInteger.ZERO.intValue()));
        Assert.assertEquals(BigInteger.ONE.intValue(), zeroCell.getX());
    }

    @Test
    public void plantBomb() throws Exception {
        Space space = new Space(BigInteger.ZERO.intValue(), BigInteger.ZERO.intValue());
        arena.plantBomb(space);
        Assert.assertTrue(space.isMined());
    }

    @Test
    public void detonateBomb() throws Exception {
        Space space = new Space(BigInteger.ZERO.intValue(), BigInteger.ZERO.intValue());
        arena.plantBomb(space);
        arena.detonateBomb(BigInteger.ZERO.intValue(), BigInteger.ZERO.intValue());
        Assert.assertTrue(arena.getCellAt(BigInteger.ZERO.intValue(), BigInteger.ZERO.intValue()).isFlaming());
    }

    @Test
    public void getAllCells() throws Exception {
        Assert.assertThat(arena.getAllCells(), is(not(empty())));
    }

    @Test
    public void getMapLock() throws Exception {
        Assert.assertNotNull(arena.getMapLock());
    }

    @Test
    public void getCompositeLock() throws Exception {
        Assert.assertNotNull(arena.getCompositeLock(arena.getCellAt(BigInteger.ZERO.intValue(), BigInteger.ZERO.intValue())));
    }

    @Test
    public void toStringArray() throws Exception {
        String[][] cells = arena.toStringArray();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                String id = cells[i][j].split(":")[0];
                Assert.assertEquals(arena.getCellAt(i, j).getId(), id);
            }
        }
    }

    @Test
    public void toGridCellsArray() throws Exception {
        GridCell[][] cells = arena.toGridCellsArray();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Cell cellAt = arena.getCellAt(i, j);
                GridCell gridCell = cells[i][j];
                Assert.assertEquals(cellAt.getX(), gridCell.getX());
                Assert.assertEquals(cellAt.getY(), gridCell.getY());
            }
        }
    }

    @Test
    public void isStartCell() throws Exception {
        Assert.assertTrue(arena.isStartCell(BigInteger.ZERO.intValue(), BigInteger.ZERO.intValue()));
    }

    @Test
    public void isCornerCell() throws Exception {
        Assert.assertTrue(arena.isCornerCell(BigInteger.ZERO.intValue(), BigInteger.ZERO.intValue()));
        Assert.assertTrue(arena.isCornerCell(BigInteger.ZERO.intValue(), height - 1));
        Assert.assertTrue(arena.isCornerCell(width - 1, BigInteger.ZERO.intValue()));
        Assert.assertTrue(arena.isCornerCell(width - 1, height - 1));
    }

    @Test
    public void isCriticalCell() throws Exception {
        Assert.assertTrue(arena.isCriticalCell(BigInteger.ZERO.intValue(), BigInteger.ONE.intValue()));
        Assert.assertTrue(arena.isCriticalCell(BigInteger.ONE.intValue(), BigInteger.ZERO.intValue()));
    }

    @Test
    public void isUnevenCell() throws Exception {
        Assert.assertTrue(arena.isUnevenCell(BigInteger.ONE.intValue(), BigInteger.ONE.intValue()));
    }
}