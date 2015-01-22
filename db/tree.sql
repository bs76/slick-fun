CREATE TABLE tree(id INTEGER PRIMARY KEY ASC,parent INTEGER,label VARCHAR(10));



INSERT INTO tree (parent,label) VALUES (NULL,"root"); 
INSERT INTO tree (parent,label) VALUES (1,"level1"); 
INSERT INTO tree (parent,label) VALUES (2,"level2"); 
INSERT INTO tree (parent,label) VALUES (3,"level3");

INSERT INTO tree (parent,label) VALUES (2,"level21"); 
INSERT INTO tree (parent,label) VALUES (2,"level22"); 
INSERT INTO tree (parent,label) VALUES (3,"level31");

WITH RECURSIVE tree1(id,parent) AS (
    VALUES(3,2)
    UNION ALL 
    SELECT tree.id,tree.parent FROM tree,tree1 WHERE tree1.parent=tree.id)
SELECT * FROM tree1;


