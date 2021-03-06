const tileSize = 40;

var tiles = {};

function createPlayer(i, j) {
    return Crafty.e('2D, Canvas, bomber, SpriteAnimation')
        .reel("walking", 1000, [
            [0, 0], [1, 0], [2, 0], [3, 0], [4, 0], [5, 0], [6, 0],
            [0, 1]
        ])
        .animate("walking", -1)
        .attr({
            x: i * tileSize + tileSize,
            y: j * tileSize,
            w: tileSize,
            h: tileSize * 2
        })
        .bind('KeyUp', function (e) {
            if (e.key == Crafty.keys.LEFT_ARROW) {
                $.getJSON('moveLeft');
            } else if (e.key == Crafty.keys.RIGHT_ARROW) {
                $.getJSON('moveRight');
            } else if (e.key == Crafty.keys.UP_ARROW) {
                $.getJSON('moveUp');
            } else if (e.key == Crafty.keys.DOWN_ARROW) {
                $.getJSON('moveDown');
            } else if (e.key == Crafty.keys.SPACE) {
                $.getJSON('plantBomb');
            }
        });
}

function createBot(i, j) {
    return Crafty.e('2D, Canvas, creep, SpriteAnimation')
        .reel("walking", 1000, [
            [0, 0], [1, 0], [2, 0],
            [0, 1], [1, 1], [2, 1]
        ])
        .animate("walking", -1)
        .attr({
            x: i * tileSize + tileSize,
            y: j * tileSize + tileSize,
            w: tileSize,
            h: tileSize
        });
}

function createBlock(i, j) {
    return Crafty.e('2D, Canvas, block').attr({
        x: i * tileSize + tileSize,
        y: j * tileSize + tileSize,
        w: tileSize,
        h: tileSize
    });
}

function createBorderBlock(i, j) {
    return Crafty.e('2D, Canvas, block').attr({
        x: i * tileSize,
        y: j * tileSize,
        w: tileSize,
        h: tileSize
    });
}

function createBox(i, j) {
    return Crafty.e('2D, Canvas, box').attr({
        x: i * tileSize + tileSize,
        y: j * tileSize + tileSize,
        w: tileSize,
        h: tileSize
    });
}

function createSpace(i, j) {
    return Crafty.e('2D, Canvas, space').attr({
        x: i * tileSize + tileSize,
        y: j * tileSize + tileSize,
        w: tileSize,
        h: tileSize
    });
}

function createBomb(i, j) {
    return Crafty.e('2D, Canvas, bomb, SpriteAnimation')
        .reel("bombing", 1000, [
            [0, 0], [1, 0],
            [0, 1]
        ])
        .animate("bombing", -1)
        .attr({
            x: i * tileSize + tileSize,
            y: j * tileSize + tileSize,
            w: tileSize,
            h: tileSize
        });
}

function createFlame(i, j) {
    return Crafty.e('2D, Canvas, flame, SpriteAnimation')
        .reel("flaming", 200, [
            [0, 0], [1, 0], [2, 0],
            [0, 1], [1, 1], [2, 1]
        ])
        .animate("flaming", 1)
        .attr({
            x: i * tileSize + tileSize,
            y: j * tileSize + tileSize,
            w: tileSize,
            h: tileSize
        });
}

function createItem(type, id, mined, flaming, i, j) {
    switch (type) {
        case 'Player':
            tiles[id] = createPlayer(i, j);
            break;
        case 'Bot':
            tiles[id] = createBot(i, j);
            break;
        case 'Block':
            tiles[id] = createBlock(i, j);
            break;
        case 'Box':
            tiles[id] = createBox(i, j);
            break;
        case 'Flame':
            tiles[id] = createFlame(i, j);
            break;
        default:
            if (mined) {
                tiles[id] = createBomb(i, j);
            } else if (flaming) {
                tiles[id] = createFlame(i, j);
            } else {
                tiles[id] = createSpace(i, j);
            }
    }
}

function drawArena(data) {
    var width = data.length;
    var height = data[0].length;

    for (var i = 0; i < width + 2; i++) {
        for (var j = 0; j < height + 2; j++) {
            if (i == 0 || j == 0) {
                createBorderBlock(i, j);
            }
            if (i == width + 1 || j == height + 1) {
                createBorderBlock(i, j);
            }
        }
    }

    for (i = 0; i < width; i++) {
        for (j = 0; j < height; j++) {
            createSpace(i, j);
        }
    }

    for (i = 0; i < width; i++) {
        for (j = 0; j < height; j++) {
            var element = data[i][j].split(':');
            var id = element[0];
            var type = element[1];
            createItem(type, id, false, false, i, j);
        }
    }
}

var assetsObj = {
    "sprites": {
        "Sprites/Blocks/sprite.png": {
            tile: 64,
            tileh: 64,
            paddingX: 1,
            paddingY: 1,
            map: {
                space: [0, 0],
                box: [1, 0],
                portal: [2, 0],
                block: [0, 1]
            }
        },
        "Sprites/Bomberman/Front/sprite.png": {
            tile: 64,
            tileh: 128,
            paddingX: 1,
            paddingY: 1,
            map: {
                bomber: [0, 0]
            }
        },
        "Sprites/Creep/Front/sprite.png": {
            tile: 64,
            tileh: 64,
            paddingX: 1,
            paddingY: 1,
            map: {
                creep: [0, 0]
            }
        },
        "Sprites/Bomb/sprite.png": {
            tile: 48,
            tileh: 48,
            paddingX: 1,
            paddingY: 1,
            map: {
                bomb: [0, 0]
            }
        },
        "Sprites/Flame/sprite.png": {
            tile: 48,
            tileh: 48,
            paddingX: 1,
            paddingY: 1,
            map: {
                flame: [0, 0]
            }
        }
    }
};

Crafty.load(assetsObj);

$.get('/newGame');

$.getJSON('/getArena', function (data) {

    var width = data.length;
    var height = data[0].length;

    Crafty.init((width + 2) * tileSize, (height + 2) * tileSize, document.getElementById('arena'));

    drawArena(data);

    var gameOver = false;

    if (!!window.EventSource) {
        var source = new EventSource('updateStatus');
        source.addEventListener('message', function (e) {
            if (e.data) {
                var data = JSON.parse(e.data);
                for (var i = 0; i < data.length; i++) {
                    var item = data[i];
                    if (item.id == 'GAME_OVER') {
                        gameOver = true;
                        continue;
                    }
                    if (tiles[item.id]) {
                        tiles[item.id].destroy();
                    }
                    if (!(item.x == -1 || item.y == -1)) {
                        createItem(item.type, item.id, item.mined, item.flaming, item.x, item.y);
                    }
                }
                if (gameOver) {
                    source.close();
                    setTimeout(function () {
                        var r = confirm("Game over :) Restart?");
                        if (r == true) {
                            window.location.replace('/');
                        }
                    }, 1000);
                }
            }
        }, false);

        source.addEventListener('error', function(e) {
            if (e.readyState == EventSource.CLOSED) {
                source.close();
            }
        }, false);
    }
});