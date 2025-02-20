package cordori.dpstamina.hook;


import cordori.dpstamina.Main;
import cordori.dpstamina.manager.ConfigManager;
import cordori.dpstamina.data.PlayerData;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class PAPIHook extends PlaceholderExpansion {

    // 使用Map来映射类型和处理函数
    private static final Map<String, BiFunction<PlayerData, String, String>> MAP_HANDLERS = new ConcurrentHashMap<>();

    static {
        // 初始化映射关系
        MAP_HANDLERS.put("usedaycount", (data, key) -> String.valueOf(data.getMapCountMap().get(key).getDayCount()));
        MAP_HANDLERS.put("useweekcount", (data, key) -> String.valueOf(data.getMapCountMap().get(key).getWeekCount()));
        MAP_HANDLERS.put("usemonthcount", (data, key) -> String.valueOf(data.getMapCountMap().get(key).getMonthCount()));

        MAP_HANDLERS.put("remaindaycount", (data, key) -> calculateRemaining(
                data.getMapCountMap().get(key).getDayCount(),
                ConfigManager.mapMap.get(key).getDayLimit()));

        MAP_HANDLERS.put("remainweekcount", (data, key) -> calculateRemaining(
                data.getMapCountMap().get(key).getWeekCount(),
                ConfigManager.mapMap.get(key).getWeekLimit()));

        MAP_HANDLERS.put("remainmonthcount", (data, key) -> calculateRemaining(
                data.getMapCountMap().get(key).getMonthCount(),
                ConfigManager.mapMap.get(key).getMonthLimit()));

        MAP_HANDLERS.put("daylimit", (data, key) -> String.valueOf(ConfigManager.mapMap.get(key).getDayLimit()));
        MAP_HANDLERS.put("weeklimit", (data, key) -> String.valueOf(ConfigManager.mapMap.get(key).getWeekLimit()));
        MAP_HANDLERS.put("monthlimit", (data, key) -> String.valueOf(ConfigManager.mapMap.get(key).getMonthLimit()));
        MAP_HANDLERS.put("cost", (data, key) -> String.valueOf(ConfigManager.mapMap.get(key).getCost()));
        MAP_HANDLERS.put("mapname", (data, key) -> ConfigManager.mapMap.get(key).getMapName());
        MAP_HANDLERS.put("ticket", (data, key) -> ConfigManager.mapMap.get(key).getTicket());
        MAP_HANDLERS.put("allowuniversal", (data, key) -> String.valueOf(ConfigManager.mapMap.get(key).isAllowUniversal()));
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getAuthor() {
        return "Cordori";
    }

    @Override
    public  String getVersion() {
        return Main.inst.getDescription().getVersion();
    }

    @Override
    public String getIdentifier() {
        return "dps";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null || identifier.isEmpty()) return null;

        UUID uuid = player.getUniqueId();
        PlayerData playerData = ConfigManager.dataMap.get(uuid);
        if (playerData == null) return null;

        String lowerId = identifier.toLowerCase();
        switch (lowerId) {
            case "stamina":
                return formatDouble(playerData.getStamina());
            case "limit":
                return getGroupLimit(player);
            case "recover":
                return processRecover(player);
            case "group":
                return ConfigManager.getGroup(player);
            case "universalticket":
                return ConfigManager.universalTicket;
        }

        if (lowerId.contains("_")) {
            return handleMapRelated(playerData, identifier);
        }

        return null;
    }

    private String handleMapRelated(PlayerData playerData, String identifier) {
        String[] parts = identifier.split("_", 2);
        if (parts.length != 2) return null;

        String mapKey = parts[0];
        String queryType = parts[1].toLowerCase();

        if (!ConfigManager.mapMap.containsKey(mapKey)) return null;

        BiFunction<PlayerData, String, String> handler = MAP_HANDLERS.get(queryType);
        return handler != null ? handler.apply(playerData, mapKey) : null;
    }

    private static String calculateRemaining(int used, int limit) {
        return String.valueOf(Math.max(limit - used, 0));
    }

    private static String formatDouble(double value) {
        return value == (int) value ? String.valueOf((int) value) : String.valueOf(value);
    }

    private static String getGroupLimit(Player player) {
        String group = ConfigManager.getGroup(player);
        return ConfigManager.groupMap.containsKey(group) ?
                formatDouble(ConfigManager.groupMap.get(group).getLimit()) :
                "0";
    }

    private static String processRecover(Player player) {
        String group = ConfigManager.getGroup(player);
        return ConfigManager.groupMap.containsKey(group) ?
                onPAPIProcess(player, ConfigManager.groupMap.get(group).getRecover()) :
                "0";
    }

    public static String onPAPIProcess(Player player, String str) {
        return PlaceholderAPI.setPlaceholders(player, str);
    }
}
