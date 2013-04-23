
-- outgoing links from a page ------------
SELECT la.anchor, ti.title as entity , COUNT(ti.title)/(SELECT COUNT(*) FROM  wikiStat.link_anchors WHERE anchor='Pittsburgh') as cnt 	FROM  wikiStat.link_anchors as la,  wikiStat.title_2_id as ti WHERE la.target=ti.id AND la.anchor='Pittsburgh' GROUP BY ti.title ORDER BY  cnt desc;



-- incoming links to a page ------------
select distinct  l.anchor  from link_anchors l, title_2_id t where t.title='Alfred_Kleiner' and t.id=l.target;


-- TUNING STUFFS ----------

create INDEX TITLE_IDX on title_2_id (title);
create INDEX TITLE_ID__IDX on title_2_id (title, id);
create INDEX ID_IDX on title_2_id (id);

create INDEX TARGET_IDX on link_anchors (target);
create INDEX ANCHOR_IDX on link_anchors (anchor);
create INDEX TRGT_ANCR_IDX on link_anchors (target, anchor);
