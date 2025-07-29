# Brand Digital Presence Monitor - Configuration Guide

## Overview
The Brand Digital Presence Monitor is a comprehensive n8n workflow that transforms social media monitoring from a single-platform Instagram Reels tracker into a multi-platform brand analytics solution. It monitors brand presence across Instagram, Facebook, Twitter/X, LinkedIn, TikTok, and YouTube, providing AI-powered insights and competitive analysis.

## Workflow Variables Configuration

### Required Variables
Configure these variables in the "Workflow Variables" node:

#### Brand Configuration
- **brand_name**: Your brand/company name (e.g., "Nike", "Apple")
- **monitoring_keywords**: Comma-separated keywords to track (e.g., "nike,justdoit,sneakers")
- **platforms_enabled**: Comma-separated platforms to monitor (e.g., "instagram,facebook,twitter,linkedin,tiktok,youtube")

#### Notion Integration
- **notion_database_id**: Main database ID for storing reports
- **notion_logs_database_id**: (Optional) Separate database for execution logs

#### Analysis Settings
- **days_to_analyze**: Number of days to analyze (default: 7)
- **max_posts_per_platform**: Maximum posts to collect per platform (default: 50)
- **competitive_brands**: Comma-separated competitor names for analysis
- **report_frequency**: How often to generate reports (daily, weekly, monthly)

#### Feature Toggles
- **include_sentiment_analysis**: Enable AI sentiment analysis (true/false)
- **include_competitive_analysis**: Enable competitive analysis (true/false)
- **send_email_alerts**: Enable email alerts for performance issues (true/false)

#### Alert Configuration
- **alert_email**: Email address for performance alerts
- **performance_threshold**: Minimum performance score before alerting (default: 30)

## Required Credentials Setup

### Social Media Platform APIs

#### Instagram Basic Display API
1. Create a Facebook Developer App
2. Add Instagram Basic Display product
3. Generate access token
4. Configure in n8n: Settings > Credentials > Instagram Basic Display API

#### Facebook Graph API
1. Use the same Facebook Developer App
2. Add Facebook Login product
3. Request necessary permissions (pages_read_engagement, pages_show_list)
4. Configure in n8n: Settings > Credentials > Facebook Graph API

#### Twitter API v2
1. Apply for Twitter Developer Account
2. Create a new app in Twitter Developer Portal
3. Generate Bearer Token
4. Configure in n8n: Settings > Credentials > Twitter OAuth2 API

#### LinkedIn API
1. Create LinkedIn Developer App
2. Request Marketing API access
3. Generate OAuth2 credentials
4. Configure in n8n: Settings > Credentials > LinkedIn OAuth2 API

#### YouTube Data API
1. Create Google Cloud Project
2. Enable YouTube Data API v3
3. Create OAuth2 credentials
4. Configure in n8n: Settings > Credentials > YouTube OAuth2 API

#### TikTok API
Note: TikTok API access is limited. The workflow uses public endpoints where possible.

### AI and Notification Services

#### OpenAI API
1. Create OpenAI account
2. Generate API key
3. Configure in n8n: Settings > Credentials > OpenAI API

#### SendGrid (Email Alerts)
1. Create SendGrid account
2. Generate API key
3. Verify sender identity
4. Configure in n8n: Settings > Credentials > SendGrid API

#### Notion API
1. Create Notion integration
2. Generate integration token
3. Share target databases with integration
4. Configure in n8n: Settings > Credentials > Notion API

## Notion Database Structure

### Main Reports Database
Create a Notion database with these properties:

| Property Name | Type | Description |
|---------------|------|-------------|
| Report ID | Title | Unique identifier for each report |
| Brand Name | Text | Brand being monitored |
| Report Date | Date | When the report was generated |
| Performance Score | Number | Overall performance score (0-100) |
| Engagement Rate | Number | Average engagement rate across platforms |
| Total Engagement | Number | Sum of likes, comments, shares |
| Reach | Number | Total reach/impressions |
| Sentiment Score | Number | AI-calculated sentiment score |
| Market Position | Select | Market position (Leader, Strong Competitor, Follower, Needs Improvement) |
| Platforms | Multi-select | Platforms analyzed in this report |
| AI Insights | Text | AI-generated insights and analysis |
| Recommendations | Text | Action recommendations |
| Action Items | Text | Specific tasks and deadlines |

### Execution Logs Database (Optional)
Create a separate database for workflow execution logs:

| Property Name | Type | Description |
|---------------|------|-------------|
| Execution ID | Title | Unique execution identifier |
| Execution Date | Date | When the workflow ran |
| Status | Select | Success, Success with Warnings, Error |
| Data Quality Score | Number | Data quality assessment (0-100) |
| Errors | Text | Any errors encountered |
| Warnings | Text | Any warnings or issues |
| Next Execution | Date | Scheduled next run time |
| Processing Log | Text | Detailed execution log |

## Workflow Schedule Configuration

The workflow is configured to run daily at 8:00 AM UTC. To modify the schedule:

1. Edit the "Daily Monitoring Schedule" node
2. Modify the cron expression in the rule settings
3. Common patterns:
   - Daily at 8 AM: `0 0 8 * * *`
   - Every 6 hours: `0 0 */6 * * *`
   - Weekly on Monday: `0 0 8 * * 1`
   - Monthly on 1st: `0 0 8 1 * *`

## Error Handling and Monitoring

The workflow includes comprehensive error handling:

- **Data Quality Checks**: Validates collected data
- **API Rate Limiting**: Respects platform rate limits
- **Fallback Mechanisms**: Continues execution if one platform fails
- **Logging**: Detailed execution logs stored in Notion
- **Alerting**: Email notifications for critical issues

## Performance Optimization

### Rate Limiting
- Instagram: 200 requests/hour
- Facebook: 600 calls per app per hour
- Twitter: 300 requests per 15-minute window
- LinkedIn: Varies by endpoint
- YouTube: 10,000 quota units per day

### Data Processing
- Batch processing for multiple brands
- Incremental data collection
- Cached results for repeated analysis

### Cost Management
- OpenAI token usage optimization
- Selective AI analysis based on configuration
- Efficient data storage in Notion

## Troubleshooting

### Common Issues

#### Authentication Errors
- Verify all API credentials are valid and not expired
- Check required permissions for each platform
- Ensure OAuth2 tokens are properly refreshed

#### Data Collection Issues
- Verify brand names and keywords are correct
- Check if social media accounts are public
- Review API rate limits and quotas

#### Notion Integration Problems
- Confirm database IDs are correct
- Verify integration has access to databases
- Check property names match exactly

#### AI Analysis Failures
- Validate OpenAI API key and credits
- Check prompt length doesn't exceed limits
- Review content for policy violations

### Debugging Steps
1. Check execution logs in Notion
2. Review individual node outputs
3. Verify workflow variables are set correctly
4. Test API credentials independently
5. Monitor rate limit usage

## Customization Options

### Adding New Platforms
1. Create new HTTP Request node
2. Configure platform-specific API calls
3. Update data processing logic
4. Add platform to router conditions

### Custom Metrics
Modify the "Data Processing & Metrics" node to include additional KPIs:
- Brand mention frequency
- Share of voice vs competitors
- Influencer engagement rates
- Campaign-specific tracking

### Enhanced AI Analysis
Customize AI prompts in the "AI Sentiment & Performance Analysis" node for:
- Industry-specific insights
- Custom sentiment categories
- Competitive intelligence focus
- Crisis detection and alerts

### Report Customization
Modify the "Report Generator" node to include:
- Custom visualizations
- Industry benchmarks
- Historical trend analysis
- Executive summary formats

## Best Practices

### Security
- Use environment variables for sensitive data
- Regularly rotate API keys
- Implement least-privilege access
- Monitor for suspicious activity

### Maintenance
- Regular credential updates
- Platform API version monitoring
- Performance optimization reviews
- Data retention policy compliance

### Scaling
- Multiple brand configurations
- Team collaboration workflows
- Automated reporting schedules
- Integration with BI tools

## Support and Updates

### Version History
- v1.0.0: Initial comprehensive brand monitoring workflow
- Future versions will include additional platforms and features

### Documentation Updates
This documentation will be updated as new features are added and platform APIs evolve.

### Community Contributions
Suggestions for improvements and additional features are welcome. Please ensure all contributions maintain data privacy and security standards.