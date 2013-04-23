-- --------------------------------------------------------------------------------
-- Routine DDL
-- Note: comments before and after the routine body will not be stored by the server
-- --------------------------------------------------------------------------------
DELIMITER $$

CREATE DEFINER=`root`@`localhost` PROCEDURE `fetchStat`()
BEGIN
	DECLARE no_more_rows1 boolean DEFAULT FALSE;
	DECLARE no_more_rows2 boolean DEFAULT FALSE;

	DECLARE done INT DEFAULT 0;

	DECLARE ANC varchar(100);	
	DECLARE ANC2 varchar(100);	
	DECLARE ENTITY varchar(100);
	DECLARE TARGET int;
	DECLARE FREQ double;


	DECLARE FirstCursor CURSOR FOR 
		SELECT DISTINCT laa.anchor, laa.target 
		FROM wikiStat.link_anchors as laa ;

	DECLARE SecondCursor CURSOR FOR 
				SELECT la.anchor, ti.title as entity , COUNT(ti.title)/(SELECT COUNT(*) FROM  wikiStat.link_anchors WHERE anchor=ANC2) as cnt
				FROM  wikiStat.link_anchors as la,  wikiStat.title_2_id as ti 
				WHERE la.target=ti.id AND la.anchor=ANC2 
				GROUP BY ti.title ORDER BY  cnt desc limit 3;
	
-- Let mysql set exit_loop to true, if there are no more rows to iterate 
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

	open FirstCursor; 
	
	/*LOOP1: LOOP*/
    REPEAT
		FETCH FirstCursor into ANC2, TARGET;
		
		IF NOT done THEN
			open SecondCursor;
				BLOCK2: BEGIN
					
					DECLARE doneLangLat INT DEFAULT 0;
					DECLARE CONTINUE HANDLER FOR NOT FOUND SET doneLangLat = 1;
					
					REPEAT
						FETCH SecondCursor into ANC, ENTITY, FREQ;

						INSERT INTO wikiStat.wikilinks(anchor,entity,freq) VALUES(ANC, ENTITY, FREQ);				
					
					UNTIL doneLangLat END REPEAT;
				END BLOCK2;
			close SecondCursor;
		END IF;
	/*END LOOP LOOP1;	*/
	UNTIL done END REPEAT; 
	close FirstCursor;

END