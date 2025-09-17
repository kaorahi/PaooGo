/**
 * gobandroid
 * by Marcus -Ligi- Bueschleb
 * http://ligi.de
 * <p/>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation;
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 **/

package org.ligi.gobandroid_hd.logic;

import androidx.annotation.NonNull;

import timber.log.Timber;

/**
 * Class to help with GTP ( Go Text Protocol )
 *
 * @author <a href="http://ligi.de">Marcus -Ligi- Bueschleb</a>
 *         <p/>
 *         This software is licenced with GPLv3
 */
public class GTPHelper {

    /**
     * @param gtp_str - the GTP string to process
     * @param game    - the game on which we execute the commands
     * @return - true if we understood the command / false if there was a
     * problem
     */
    public static boolean doMoveByGTPString(String gtp_str, GoGame game) {

        Timber.i("processing gtp str" + gtp_str);

        // remove chars we do not need
        for (String c : new String[]{" ", "=", "\r", "\n", "\t"}) {
            gtp_str = gtp_str.replace(c, "");
        }

        if (gtp_str.toUpperCase().equals("RESIGN")) {
            game.pass(); // TODO handle this case better
            return true;
        } else if (gtp_str.toUpperCase().equals("PASS")) {
            game.getActMove().setComment(game.getActMove().getComment() + "passes");
            game.pass();
            return true;
        }

        try {

            final Cell boardCell = strToCell(gtp_str, game);

            game.do_move(boardCell); // internal here?
            return true;
        } catch (Exception e) {
            Timber.w(e, "Problem parsing coordinates from GTP");
        }

        // if we got here we could not make sense of the command
        Timber.w("could not make sense of the GTP command: " + gtp_str);
        return false;

    }

    @NonNull
    public static Cell strToCell(String gtp_str, GoGame game) {
        final byte y = (byte) (game.getBoardSize() - (Byte.parseByte(gtp_str.substring(1))));
        byte x = (byte) (gtp_str.charAt(0) - 'A');

        if (x > 8) {
            x--; // the I is missing ^^ - took me some time to find that out
        }

        return game.getCalcBoard().getCell(x, y);
    }

    public static String coordinates2gtpstr(final Cell cell, final int gameSize) {

        // "I" is missing decrease human OCR-error but increase computer bugs ...
        final int x_offset = (cell.getX() >= 8) ? 1 : 0;
        return "" + (char) ('A' + cell.getX() + x_offset) + "" + (gameSize - cell.getY());
    }
}
