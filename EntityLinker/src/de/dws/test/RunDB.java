package de.dws.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import de.dws.mapper.dbConnectivity.DBConnection;

public class RunDB
{
    // define Logger
    static Logger logger = Logger.getLogger(RunDB.class.getName());

    static// DB connection instance, one per servlet
    Connection connection = null;

    // prepared statement instance
    static PreparedStatement pstmt = null;

    static PreparedStatement pstmt2 = null;

    static Statement stmt = null;

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try {
            DBConnection dbc = new DBConnection();

            // retrieve the freshly created connection instance
            connection = dbc.getConnection();

            stmt = connection.createStatement();

            ResultSet rs1 =
                stmt.executeQuery("select la.anchor, la.target from " + "wikiStat.link_anchors as la ");

            pstmt =
                connection.prepareStatement("SELECT la.anchor as anc, ti.title as entity, "
                    + "(COUNT(ti.title)/(SELECT COUNT(*) FROM  wikiStat.link_anchors where anchor = ?)) " + "as cnt "
                    + "FROM  wikiStat.link_anchors as la,  wikiStat.title_2_id as ti "
                    + "WHERE la.target=ti.id  AND la.anchor= ? " + "GROUP BY ti.title ORDER BY  cnt desc limit 3");

            pstmt2 = connection.prepareStatement("INSERT INTO wikiStat.stats (anchor, entity,freq) VALUES (? , ?, ?)");


            ResultSet rs = null;

            String anchor;
            String anc;
            String entity;
            double freq;

            while (rs1.next()) {
                anchor = rs1.getString("anchor");
                // int target = rs.getInt("target");
                // logger.info(anchor + "  " + target);
                pstmt.setString(1, anchor);
                pstmt.setString(2, anchor);

                rs = pstmt.executeQuery();

                while (rs.next()) {
                    anc = rs.getString("anc");
                    entity = rs.getString("entity");
                    freq = rs.getDouble("cnt");
                    logger.info(anc + "  " + entity + " " + freq);

                    pstmt2.setString(1, anc);
                    pstmt2.setString(2, entity);
                    pstmt2.setDouble(3, freq);

                    pstmt2.executeUpdate();

                }
                
                pstmt2.clearParameters();

                pstmt.clearParameters();

            }

        } catch (SQLException e) {
            logger.error("shit .." + e.getMessage());
        }

    }

}
