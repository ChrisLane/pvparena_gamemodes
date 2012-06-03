package net.slipcor.pvparena.arenas.domination;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Debug;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.managers.Teams;

public class DominationRunnable implements Runnable {
	public final boolean take;
	public final Location loc;
	public int ID = -1;
	private final Arena arena;
	public final String team;
	private Debug db = new Debug(39);
	private final Domination domination;

	/**
	 * create a domination runnable
	 * 
	 * @param a
	 *            the arena we are running in
	 * @param domination 
	 */
	public DominationRunnable(Arena a, boolean b, Location l, String s, Domination d) {
		arena = a;
		take = b;
		team = s;
		loc = l;
		domination = d;
		db.i("Domination constructor");
	}
	

	/**
	 * the run method, commit arena end
	 */
	@Override
	public void run() {
		db.i("DominationRunnable commiting");
		db.i("team " + team + ", take: " + String.valueOf(take));
		if (take) {
			// claim a flag for the team
			if (domination.paFlags.containsKey(loc)) {
				db.i("flag claimed. add score!");
				// flag claimed! add score!
				arena.type().reduceLivesCheckEndAndCommit(team);
				arena.tellEveryone(
						Language.parse("domscore", Teams.getTeam(arena, team).colorize()
								+ ChatColor.YELLOW));
			} else {
				// flag unclaimed! claim!
				db.i("clag unclaimed. claim!");
				domination.paFlags.put(loc, team);
				long interval = 20L * 5;
				
				arena.tellEveryone(
						Language.parse("domclaiming", Teams.getTeam(arena, team).colorize()
								+ ChatColor.YELLOW));
				
				DominationRunnable running = new DominationRunnable(arena,
						take, loc, team, domination);
				running.ID = Bukkit.getScheduler().scheduleSyncRepeatingTask(
						PVPArena.instance, running, interval, interval);
				domination.paRuns.put(loc, running);
			}
		} else {
			// unclaim
			db.i("unclaim");
			if (domination.paRuns.containsKey(loc)) {
				arena.tellEveryone(
						Language.parse("domunclaiming", domination.paRuns.get(loc).team
								+ ChatColor.YELLOW));
				
				int run_id = domination.paRuns.get(loc).ID;
				Bukkit.getScheduler().cancelTask(run_id);
			}
		}
	}

	public boolean noOneThere(int checkDistance) {
		for (ArenaPlayer p : arena.getPlayers()) {
			if (p.get().getLocation().distance(loc) < checkDistance) {
				return false;
			}
		}
		return true;
	}
}
