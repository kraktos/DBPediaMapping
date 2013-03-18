SELECT t.title as entity, COUNT(t.title)/(SELECT COUNT(*) FROM
wikiStat.link_anchors WHERE anchor='beckham') as cnt FROM wikiStat.link_anchors as la,
wikiStat.title_2_id as t WHERE la.target=t.id AND la.anchor='beckham' GROUP BY
t.title ORDER BY  cnt desc limit 5;