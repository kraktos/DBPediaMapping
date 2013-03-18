select anchor from wikiStat.link_anchors limit 4;  
       
        
--SELECT t.title as entity, COUNT(t.title)/(SELECT COUNT(*) FROM  wikiStat.link_anchors WHERE anchor='pauli') 
--as cnt FROM  wikiStat.link_anchors as la,  wikiStat.title_2_id as t WHERE la.target=t.id AND la.anchor='pauli' 
--GROUP BY t.title ORDER BY  cnt desc limit 5;
        
      
        