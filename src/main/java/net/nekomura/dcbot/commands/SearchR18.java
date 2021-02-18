package net.nekomura.dcbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.nekomura.dcbot.Config;
import net.nekomura.dcbot.Enums.ConfigJsonArrayData;
import net.nekomura.dcbot.Enums.ConfigStringData;
import net.nekomura.dcbot.commands.Managers.CommandContext;
import net.nekomura.dcbot.commands.Managers.ICommand;
import net.nekomura.utils.jixiv.artworks.Illustration;
import net.nekomura.utils.jixiv.enums.artwork.PixivImageSize;
import net.nekomura.utils.jixiv.enums.search.*;
import net.nekomura.utils.jixiv.IllustrationInfo;
import net.nekomura.utils.jixiv.Pixiv;
import net.nekomura.utils.jixiv.SearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SearchR18 implements ICommand {
    @Override
    public void handle(CommandContext ctx) throws Exception {
        if (!ctx.event.getChannel().isNSFW()) {
            EmbedBuilder eb = new EmbedBuilder().setColor(Integer.parseInt(Config.get(ConfigStringData.EMBED_MESSAGE_COLOR), 16));
            eb.setDescription("此指令僅能在限制級頻道使用。");
            ctx.event.getChannel().sendMessage(eb.build()).queue();
        }else {
            if (ctx.args.size() == 0) {
                EmbedBuilder eb = new EmbedBuilder().setColor(Integer.parseInt(Config.get(ConfigStringData.EMBED_MESSAGE_COLOR),16));
                eb.setDescription("請輸入搜尋關鍵字。");
                ctx.event.getChannel().sendMessage(eb.build()).queue();
                return;
            }

            StringBuilder order_keyword = new StringBuilder();
            for (String s : ctx.args) {
                order_keyword.append(s).append(" ");
            }

            PixivSearchMode mode = PixivSearchMode.R18;

            ArrayList<Object> keywords = Config.jsonArrayToArrayList(Config.get(ConfigJsonArrayData.RANDOM_PIXIV_SEARCH_KEYWORDS));
            String keyword = (String) keywords.get(ThreadLocalRandom.current().nextInt(0, keywords.size()));
            boolean contain = false;

            for (Object o : keywords) {
                if (ctx.args.contains(o)) {
                    contain = true;
                    break;
                }
            }

            if (!contain) {
                order_keyword.append(keyword).append(" ");
            }

            PixivSearchOrder order = PixivSearchOrder.NEW_TO_OLD;
            SearchResult tempResult = Pixiv.search(order_keyword.toString(), 1, PixivSearchArtworkType.Illustrations, order, mode, PixivSearchSMode.S_TAG, PixivSearchType.Illust);

            if (tempResult.getResultCount() == 0) {
                EmbedBuilder eb = new EmbedBuilder().setColor(Integer.parseInt(Config.get(ConfigStringData.EMBED_MESSAGE_COLOR),16));
                eb.setDescription("沒有隨機搜尋到圖。請重試或是新增收藏人數關鍵字(如\"2000users入り\"即為2000用戶收藏，目前可使用的數值有100、200、500、1000、2000、5000、10000、20000、50000)");
                ctx.event.getChannel().sendMessage(eb.build()).queue();
                return;
            }

            int lastPage = tempResult.getLastPageIndex();
            int page = ThreadLocalRandom.current().nextInt(1, lastPage + 1);
            SearchResult result = Pixiv.search(order_keyword.toString(), page, PixivSearchArtworkType.Illustrations, order, mode, PixivSearchSMode.S_TAG, PixivSearchType.Illust);

            int max = result.getPageResultCount();
            int artworkID = result.getIds()[ThreadLocalRandom.current().nextInt(0, max)];

            IllustrationInfo info = Illustration.getInfo(artworkID);

            int artworkPage = ThreadLocalRandom.current().nextInt(0, info.getPageCount());
            byte[] image = info.getImage(artworkPage, PixivImageSize.Original);
            if (image.length > 8388608) {
                image = info.getImage(artworkPage, PixivImageSize.Regular);
            }
            String type = info.getImageFileFormat(artworkPage);

            EmbedBuilder eb = new EmbedBuilder().setColor(Integer.parseInt(Config.get(ConfigStringData.EMBED_MESSAGE_COLOR), 16));
            eb.setTitle("隨機搜尋pixiv圖片");
            eb.addField("ID", "[" + artworkID + "](https://www.pixiv.net/artworks/" + artworkID + ")", true);
            eb.setImage("attachment://" + info.getId() + "." + type);
            ctx.event.getChannel().sendFile(image, info.getId() + "." + type).embed(eb.build()).queue();
        }
    }

    @Override
    public String getName() {
        return "searchR18";
    }

    @Override
    public List<String> getAliases() {
        String[] aliases = {"r18Search"};
        return Arrays.asList(aliases);
    }
}
