package prosta.ukrmin.gamemodes;

import arc.math.Rand;
import arc.util.Log;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;

public class TheMines {
    public static void init() {
        activateRules();

        Timer.schedule(TheMines::run, 1, 3);
    }

    private static void test () {
        int i = 0;
        for (Tile tile : Vars.world.tiles) {
            if (tile.block() instanceof CoreBlock core && tile.isCenter()) {
                System.out.println( i++ + " " + tile.getX() + " " + tile.getY());
                core.unitType = UnitTypes.crawler;
            }
        }

    }
    private static int random(int min, int max) {
        Rand rand = new Rand();

        return rand.random(min, max);
    }

    private static void run() {
        if (Vars.state.isPlaying() && !Vars.state.serverPaused) {
            test();
            Block trash = Blocks.scrapWall;
            Building building;
            int w = random(0, Vars.world.width());
            int h = random(0, Vars.world.height());
            Log.info(w + "|" + h);
            Log.info(Vars.world.tiles.get(w, h));
            Vars.world.build(w, h);
            Call.setTile(new Tile(w, h), trash, Team.green, 0);
            Log.info(Vars.world.tiles.get(w, h));
            ItemStack[] is;
            is = new ItemStack[1];
            is[0] = new ItemStack(Items.copper, 300);
//            Block block = Vars.world.tileBuilding();

            Blocks.battery.requirements = is;
//            activateRules();
        }
    }

    private static void activateRules() {
        UnitTypes.alpha.flying = false;
        UnitTypes.alpha.init();
//        UnitTypes.alpha.app

//        CoreBlock.
//        Blocks.coreFoundation.unitType =
//        Blocks.coreShard.unitType = UnitTypes.mega;
//        Blocks.coreFoundation.unitType = UnitTypes.mega;
//        Blocks.coreNucleus.unitType = UnitTypes.mega;
        Vars.state.rules.bannedUnits.addAll(
                UnitTypes.flare,
                UnitTypes.horizon,
                UnitTypes.zenith,
                UnitTypes.antumbra,
                UnitTypes.eclipse,
                UnitTypes.mono,
                UnitTypes.poly,
                UnitTypes.mega,
                UnitTypes.quad,
                UnitTypes.oct,
//                    UnitTypes.alpha,
                UnitTypes.beta,
                UnitTypes.gamma
        );
    }

    public void registerClientCommands(CommandHandler handler) {

        handler.<Player>register("core", "creates a new core block", (strings, player) -> {
            Iterable<NetConnection> connections = Vars.net.getConnections();

            // перевірити дистанцію між ядрами, якщо ядро знайдене - повідомити гравця та закінчити виконання коду
            for(NetConnection conn: connections) {
                double d = Math.sqrt(((player.x - conn.player.core().x) * (player.x - conn.player.core().x)) + ((player.y - conn.player.team().core().y) * (player.y - conn.player.team().core().y)));

                Log.info("Distance: " + d);

                if(d <= 150) {
                    player.sendMessage("[red]У вашому радіусі знайдений ворожий блок, будь-ласка знайдіть інше місце.");
                    return;
                }
            }

            // якщо ж у радіусі гравця ядрів немає тоді перевірити на кількість ресурсів в головному ядрі та зробити все необхідне
            if(player.team().core().items().get(Items.copper) >= 1500 & player.team().core().items().get(Items.lead) >= 800) {

                // видалити запаси з ядра
                player.team().core().removeStack(Items.copper, 1500);
                player.team().core().removeStack(Items.lead, 800);

                // побудувати ядро та повідомити гравця про успішне виконання комманди
                // TODO реалізувати будування ядра
                player.sendMessage("[green]Ядро створене!");

                return;

                // якщо у головному ядрі не знайдено необхідну кількість ресурсів тоді перевірити на кількість ядер, якщо їх більше одного тоді шукати у кожному ресурси
            } else if(player.team().cores().size > 1) {
                for(CoreBlock.CoreBuild coreBuild: player.team().cores()) {

                    // перевірка на ресурси, якщо все що потрібно є тоді виконати те що було у першій перевірці
                    if(coreBuild.items().get(Items.copper) >= 1500 & coreBuild.items().get(Items.lead) >= 800) {

                        coreBuild.removeStack(Items.copper, 1500);
                        coreBuild.removeStack(Items.lead, 800);


                        // TODO реалізувати будування ядра
                        player.sendMessage("[green]Ядро створене!");
                        return;
                    }
                }
            }

            // якщо ж жодних попередніх подій не виконується тоді все дійде до цього фрагрменту коду.
            player.sendMessage("[red]У ядрі не достатньо ресурсів!\n[green]Необхідні ресурси: 1500 Міді, 800 Свинцю.");
        });
    }
}
