package jp.cssj.sakae;

public interface GCConstants {
	/** 1インチ辺りのポイント数。 */
	public static final double POINTS_PER_INCH = 72.0;

	/** 1mm辺りのポイント数。 */
	public static final double POINTS_PER_MM = POINTS_PER_INCH / 25.4;

	/** 1cm辺りのポイント数。 */
	public static final double POINTS_PER_CM = POINTS_PER_INCH / 2.54;

	public static final double PAPER_A4_WIDTH_MM = 210.0;

	public static final double PAPER_A4_HEIGHT_MM = 297.0;

	public static final double CUTTING_MARGIN_MM = 3.0;
}
