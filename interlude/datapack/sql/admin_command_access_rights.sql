-- -----------------------------------------------
-- Table structure for admin_command_access_rights
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS `admin_command_access_rights` (
  `adminCommand` varchar(255) NOT NULL DEFAULT 'admin_',
  `accessLevels` varchar(255) NOT NULL,
  PRIMARY KEY  (`adminCommand`)
) DEFAULT CHARSET=utf8;
-- ---------------------------------------------
-- Records for table admin_command_access_rights
-- ---------------------------------------------

INSERT IGNORE INTO `admin_command_access_rights` VALUES 
-- Section: Admin Access
('admin_admin','1'),
('admin_admin1','1'),
('admin_admin2','1'),
('admin_admin3','1'),
('admin_admin4','1'),
('admin_admin5','1'),
('admin_admin6','1'),
('admin_config_server','1'),
('admin_config_server2','1'),
('admin_config_reload_menu','1'),
('admin_edit','1'),
('admin_changename_menu','1'),
('admin_character_info','1'),
('admin_find_ip','1'),
('admin_find_account','1'),
('admin_spawn_menu','1'),
('admin_otspawn','1'),
('admin_list_groups','1'),
('admin_delay','1'),

-- Section: Buffs
('admin_getbuffs','1'),
('admin_stopbuff','1'),
('admin_stopallbuffs','1'),
('admin_areacancel','1'),

-- Section: Auto Announcements
('admin_autoannounce','1'),
('admin_list_autoannouncements','1'),
('admin_add_autoannouncement','1'),
('admin_del_autoannouncement','1'),

-- Section: Noble
('admin_setnoble','1'),

-- Section: Donator System
('admin_setdonator','1'),

-- Section: Area Commands
('admin_areaskill','1'),
('admin_areakill','1'),

-- Section: Common Commands
('admin_effect_menu','1'),
('admin_delete','1'),
('admin_gmshop','1'),
('admin_heal','1'),
('admin_help','1'),
('admin_vis','1'),
('admin_invis','1'),
('admin_invis_menu','1'),
('admin_invul','1'),
('admin_kill','1'),
('admin_kill_monster','1'),
('admin_massress','1'),
('admin_res_monster','1'),
('admin_masskill','1'),
('admin_res','1'),
('admin_setcp','1'),
('admin_sethp','1'),
('admin_setmp','1'),
('admin_gmcancel','1'),
('admin_save_stats','1'),
('admin_gmspeed','1'),
('admin_gmspeed_menu','1'),
('admin_silence','1'),
('admin_target','1'),
('admin_targets','1'),
('admin_close_window','1'),
('admin_load_npc','1'),
('admin_diet_on','1'),
('admin_diet_off','1'),
('admin_monster_race','1'),
('admin_diet','1'),
('admin_fullfood','1'),
('admin_sendhome','1'),
('admin_bookmark','1'),
('admin_failed','1'),
('admin_fcs','1'),
('admin_region_check','1'),
('admin_camera','1'),

-- Section: Char Management
('admin_edit_quest','1'),
('admin_edit_stats','1'),
('admin_edit_class','1'),
('admin_ban','1'),
('admin_banchat','1'),
('admin_unbanchat','1'),
('admin_banchat_all','1'),
('admin_unbanchat_all','1'),
('admin_jail','1'),
('admin_massjail','1'),
('admin_kick','1'),
('admin_unban','1'),
('admin_unjail','1'),
('admin_kick_non_gm','1'),
('admin_enchant_info','1'),
('admin_instant_move','1'),
('admin_setchar_cp','1'),
('admin_setchar_hp','1'),
('admin_setchar_mp','1'),
('admin_save_modifications','1'),

-- Section: Clan Hall
('admin_clanhall','1'),
('admin_clanhalldel','1'),
('admin_clanhallset','1'),
('admin_clanhallteleportself','1'),
('admin_clanhallopendoors','1'),
('admin_clanhallclosedoors','1'),

-- Section: Announcements
('admin_add_announcement','1'),
('admin_announce','1'),
('admin_announce_announcements','1'),
('admin_announce_menu','1'),
('admin_del_announcement','1'),
('admin_list_announcements','1'),
('admin_reload_announcements','1'),

-- Section: Reload Commands
('admin_cache_htm_rebuild','1'),
('admin_cache_htm_reload','1'),
('admin_cache_crest_fix','1'),
('admin_cache_crest_rebuild','1'),
('admin_cache_crest_reload','1'),
('admin_quest_reload','1'),
('admin_teleport_reload','1'),
('admin_spawn_reload','1'),
('admin_zone_reload','1'),
('admin_reload','1'),
('admin_reload_menu','1'),
('admin_config_reload','1'),
('admin_cache_reload_path','1'),
('admin_cache_reload_file','1'),
('admin_script_load','1'),

-- Section: Door Control
('admin_close','1'),
('admin_closeall','1'),
('admin_open','1'),
('admin_openall','1'),

-- Section: Char Modification
('admin_setclass','1'),
('admin_add_exp_sp','1'),
('admin_add_exp_sp_to_character','1'),
('admin_remove_exp_sp','1'),
('admin_add_level','1'),
('admin_addlevel','1'),
('admin_set_level','1'),
('admin_remlevel','1'),
('admin_changename','1'),
('admin_character_list','1'),
('admin_current_player','1'),
('admin_edit_character','1'),
('admin_find_character','1'),
('admin_find_dualbox','1'),
('admin_nokarma','1'),
('admin_rec','1'),
('admin_restore_character','1'),
('admin_repair_character','1'),
('admin_setew','1'),
('admin_setkarma','1'),
('admin_setcolor','1'),
('admin_setname','1'),
('admin_setsex','1'),
('admin_settitle','1'),
('admin_show_characters','1'),
('admin_sethero','1'),
('admin_remclanwait','1'),
('admin_setinvul','1'),
('admin_clear_inventory','1'),

-- Section: Edit Npc
('admin_edit_npc','1'),
('admin_save_npc','1'),
('admin_show_droplist','1'),
('admin_edit_drop','1'),
('admin_add_drop','1'),
('admin_del_drop','1'),
('admin_showShop','1'),
('admin_showCustomShop','1'),
('admin_showShopList','1'),
('admin_showCustomShopList','1'),
('admin_addShopItem','1'),
('admin_delShopItem','1'),
('admin_addCustomShopItem','1'),
('admin_delCustomShopItem','1'),
('admin_editShopItem','1'),
('admin_editCustomShopItem','1'),
('admin_show_skilllist_npc','1'),
('admin_edit_skill_npc','1'),
('admin_add_skill_npc','1'),
('admin_del_skill_npc','1'),

-- Section: Effects
('admin_atmosphere','1'),
('admin_atmosphere_menu','1'),
('admin_invisible','1'),
('admin_visible','1'),
('admin_earthquake','1'),
('admin_earthquake_menu','1'),
('admin_bighead','1'),
('admin_shrinkhead','1'),
('admin_unpara_all','1'),
('admin_para_all','1'),
('admin_unpara','1'),
('admin_para','1'),
('admin_polymorph','1'),
('admin_unpolymorph','1'),
('admin_polyself','1'),
('admin_unpolyself','1'),
('admin_effect','1'),
('admin_social','1'),
('admin_play_sounds','1'),
('admin_play_sound','1'),
('admin_abnormal','1'),
('admin_polyself_menu','1'),
('admin_abnormal_menu','1'),
('admin_unpolyself_menu','1'),
('admin_polymorph_menu','1'),
('admin_unpolymorph_menu','1'),
('admin_unpara_all_menu','1'),
('admin_para_all_menu','1'),
('admin_unpara_menu','1'),
('admin_para_menu','1'),
('admin_clearteams','1'),
('admin_setteam_close','1'),
('admin_setteam','1'),

-- Section: Enchant Menu
('admin_seteh','1'),
('admin_setec','1'),
('admin_seteg','1'),
('admin_setel','1'),
('admin_seteb','1'),
('admin_setes','1'),
('admin_setle','1'),
('admin_setre','1'),
('admin_setlf','1'),
('admin_setrf','1'),
('admin_seten','1'),
('admin_setun','1'),
('admin_setba','1'),
('admin_enchant','1'),

-- Section: Olympiad
('admin_saveolymp','1'),
('admin_manualhero','1'),
('admin_endolympiad','1'),

-- Section: Server Settings
('admin_server_shutdown','1'),
('admin_server_restart','1'),
('admin_server_abort','1'),
('admin_server_gm_only','1'),
('admin_server_all','1'),
('admin_server_max_player','1'),
('admin_server_list_clock','1'),
('admin_server_login','1'),
('admin_rateinfo','1'),
('admin_give_souls','1'),
('admin_enchantinfo','1'),

-- Section: Menus
('admin_char_manage','1'),
('admin_teleport_character_to_menu','1'),
('admin_recall_char_menu','1'),
('admin_recall_party_menu','1'),
('admin_recall_clan_menu','1'),
('admin_goto_char_menu','1'),
('admin_kick_menu','1'),
('admin_kill_menu','1'),
('admin_ban_menu','1'),
('admin_unban_menu','1'),
('admin_bbs','1'),

-- Section: Mob Group Control
('admin_mobmenu','1'),
('admin_mobgroup_create','1'),
('admin_mobgroup_spawn','1'),
('admin_mobgroup_unspawn','1'),
('admin_mobgroup_kill','1'),
('admin_mobgroup_idle','1'),
('admin_mobgroup_attack','1'),
('admin_mobgroup_rnd','1'),
('admin_mobgroup_return','1'),
('admin_mobgroup_follow','1'),
('admin_mobgroup_casting','1'),
('admin_mobgroup_nomove','1'),
('admin_mobgroup_attackgrp','1'),
('admin_mobgroup_invul','1'),
('admin_mobgroup_remove','1'),
('admin_mobgroup_list','1'),
('admin_mobgroup_delete','1'),

-- Section: Path Node
('admin_pn_info','1'),
('admin_show_path','1'),
('admin_path_debug','1'),
('admin_show_pn','1'),
('admin_find_path','1'),

-- Section: Ride
('admin_ride_wyvern','1'),
('admin_ride_strider','1'),
('admin_unride_wyvern','1'),
('admin_unride_strider','1'),
('admin_unride','1'),
('admin_ride_wolf','1'),
('admin_unride_wolf','1'),

-- Section: Siege
('admin_siege','1'),
('admin_add_attacker','1'),
('admin_add_defender','1'),
('admin_add_guard','1'),
('admin_list_siege_clans','1'),
('admin_clear_siege_list','1'),
('admin_move_defenders','1'),
('admin_spawn_doors','1'),
('admin_endsiege','1'),
('admin_setsiegetime','1'),
('admin_startsiege','1'),
('admin_setcastle','1'),
('admin_remaining_time_to_end_siege','1'),
('admin_removecastle','1'),

-- Section: Skills
('admin_show_skills','1'),
('admin_remove_skills','1'),
('admin_skill_list','1'),
('admin_skill_index','1'),
('admin_add_skill','1'),
('admin_add_clan_skill','1'),
('admin_remove_skill','1'),
('admin_get_skills','1'),
('admin_reset_skills','1'),
('admin_give_all_skills','1'),
('admin_remove_all_skills','1'),
('admin_ench_skills','1'),
('admin_cast_skill','1'),

-- Section: Spawns
('admin_spawnsearch_menu','1'),
('admin_spawndelay','1'),
('admin_spawnlist','1'),
('admin_list_spawns','1'),
('admin_spawnlist_menu','1'),
('admin_cspawn','1'),
('admin_spawn_once','1'),
('admin_spawnnight','1'),
('admin_spawnday','1'),
('admin_mammon_find','1'),
('admin_mammon_respawn','1'),
('admin_show_spawns','1'),
('admin_spawn','1'),
('admin_spawn_index','1'),
('admin_spawn_monster','1'),
('admin_respawnall','1'),
('admin_unspawnall','1'),
('admin_frintezza','1'),

-- Section: Teleport
('admin_show_moves','1'),
('admin_show_moves_other','1'),
('admin_show_teleport','1'),
('admin_teleport_to_character','1'),
('admin_teleportto','1'),
('admin_move_to','1'),
('admin_delbookmark','1'),
('admin_teleport_character','1'),
('admin_recall','1'),
('admin_recall_party','1'),
('admin_massrecall','1'),
('admin_recall_gm','1'),
('admin_recall_offline','1'),
('admin_walk','1'),
('admin_explore','1'),
('admin_recall_npc','1'),
('admin_gonorth','1'),
('admin_go','1'),
('admin_gosouth','1'),
('admin_goeast','1'),
('admin_gowest','1'),
('admin_goup','1'),
('admin_godown','1'),
('admin_tele','1'),
('admin_teleto','1'),
('admin_recall_pt','1'),
('admin_recall_all','1'),
('admin_disable_gk','1'),
('admin_enable_gk','1'),

-- Section: Test Commands
('admin_known','1'),
('admin_mp','1'),
('admin_msg','1'),
('admin_docast','1'),
('admin_docastself','1'),
('admin_social_menu','1'),
('admin_heading','1'),
('admin_stats','1'),
('admin_fight_calculator','1'),
('admin_fight_calculator_show','1'),
('admin_mons','1'),
('admin_forge','1'),
('admin_forge2','1'),
('admin_forge3','1'),
('admin_zone_check','1'),
('admin_show_ai','1'),
('admin_boat','1'),

-- Section: Transformations
('admin_transform','1'),
('admin_transform_menu','1'),
('admin_untransform','1'),
('admin_untransform_menu','1'),

-- Section: Petitions
('admin_accept_petition','1'),
('admin_reject_petition','1'),
('admin_reset_petitions','1'),
('admin_view_petition','1'),
('admin_view_petitions','1'),
('admin_force_peti','1'),

-- Section: GM Settings
('admin_changelvl','1'),
('admin_gm','1'),
('admin_gmchat','1'),
('admin_gmchat_menu','1'),
('admin_gmlistoff','1'),
('admin_gmliston','1'),
('admin_snoop','1'),
('admin_unsnoop','1'),

-- Section: Miscellaneous
('admin_buy','1'),
('admin_create_item','1'),
('admin_itemcreate','1'),
('admin_create_adena','1'),
('admin_pledge','1'),
('admin_set','1'),
('admin_tradeoff','1'),
('admin_unblockip','1'),
('admin_set_menu','1'),
('admin_set_mod','1'),
('admin_sortmulti','1'),
('admin_smartshop','1'),

-- Section: Geodata
('admin_geo_z','1'),
('admin_geo_type','1'),
('admin_geo_nswe','1'),
('admin_geo_los','1'),
('admin_geo_position','1'),
('admin_geo_bug','1'),
('admin_geo_load','1'),
('admin_geo_unload','1'),
('admin_geoeditor_connect','1'),
('admin_geoeditor_join','1'),
('admin_geoeditor_leave','1'),
('admin_ge_status','1'),
('admin_ge_mode','1'),
('admin_ge_join','1'),
('admin_ge_leave','1'),
 
-- Section: Cursed Weapons
('admin_cw_info','1'),
('admin_cw_info_menu','1'),
('admin_cw_remove','1'),
('admin_cw_goto','1'),
('admin_cw_reload','1'),
('admin_cw_add','1'),

-- Section: Manor Manager Control
('admin_manor','1'),
('admin_manor_reset','1'),
('admin_manor_info','1'),
('admin_manor_setnext','1'),
('admin_manor_approve','1'),
('admin_manor_disable','1'),
('admin_manor_setmaintenance','1'),
('admin_manor_save','1'),

-- Section: Clan Actions
('admin_setclanlv','1'),

-- Section: Fort Siege
('admin_fortsiege','1'),
('admin_add_fortattacker','1'),
('admin_add_fortdefender','1'),
('admin_add_fortguard','1'),
('admin_list_fortsiege_clans','1'),
('admin_clear_fortsiege_list','1'),
('admin_move_fortdefenders','1'),
('admin_spawn_fortdoors','1'),
('admin_endfortsiege','1'),
('admin_startfortsiege','1'),
('admin_setfort','1'),
('admin_removefort','1'),

-- Section: Seven Signs 
('admin_seven_signs_cycle','1'),
('admin_seven_signs_period','1'),
('admin_festival_time','1'),
('admin_festival_start','1'),
('admin_festival_end','1'),
('admin_spawn_orators','1'),
('admin_spawn_preachers','1'),
('admin_unspawn_orators','1'),
('admin_unspawn_preachers','1'),
('admin_spawn_dusk_crest','1'),
('admin_spawn_dawn_crest','1'),
('admin_uspawn_dawn_crest','1'),
('admin_uspawn_dusk_crest','1'),
('admin_spawn_mammon','1'),
('admin_uspawn_mammon','1'),
('admin_spawn_lilith','1'),
('admin_spawn_anakim','1'),
('admin_unspawn_lilith','1'),
('admin_unspawn_anakim','1'),

-- Section: Christmas
('admin_christmas_start','1'),
('admin_christmas_end','1'),

-- Section: Instances
('admin_createinstance','1'),
('admin_destroyinstance','1'),
('admin_listinstances','1'),
('admin_setinstance','1'),
('admin_ghoston','1'),
('admin_ghostoff','1');